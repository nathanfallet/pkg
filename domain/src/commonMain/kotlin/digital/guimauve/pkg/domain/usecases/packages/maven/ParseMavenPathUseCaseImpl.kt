package digital.guimauve.pkg.domain.usecases.packages.maven

import digital.guimauve.pkg.domain.exceptions.packages.maven.InvalidMavenPathException
import digital.guimauve.pkg.domain.models.packages.maven.MavenPath

class ParseMavenPathUseCaseImpl : ParseMavenPathUseCase {
    override fun invoke(segments: List<String>): MavenPath {
        if (segments.size < 3) throw InvalidMavenPathException()
        // A path is split before its segments are decoded, so `%2e%2e%2f` reaches us as `../`
        // inside a single segment. Left unchecked it escapes the root of the storage on upload.
        // A quote or a control character would also break the headers the download route sets.
        if (segments.any { segment ->
                segment.isEmpty() || segment == "." || segment == ".." ||
                        segment.any { it == '/' || it == '\\' || it == '"' || it < ' ' }
            }
        ) throw InvalidMavenPathException()
        val filename = segments.last()

        val possibleVersion = segments[segments.size - 2]
        val possibleArtifactId = segments[segments.size - 3]
        val remainingGroupParts = segments.dropLast(3)

        val isProbablyVersion = isVersion(possibleVersion, filename)

        return if (isProbablyVersion) {
            // /group/artifact/version/filename
            val groupId = remainingGroupParts.joinToString(".")
            MavenPath(groupId, possibleArtifactId, possibleVersion, filename)
        } else {
            // /group/artifact/filename (no version)
            val artifactId = possibleVersion
            val groupId = segments.dropLast(2).joinToString(".")
            MavenPath(groupId, artifactId, null, filename)
        }
    }

    private fun isVersion(versionSegment: String, filename: String): Boolean {
        // Heuristic: version should be in filename OR be a valid semver
        return filename.contains("-$versionSegment") ||
                Regex("""\d+\.\d+(\.\d+)?([-+].+)?""").matches(versionSegment)
    }
}
