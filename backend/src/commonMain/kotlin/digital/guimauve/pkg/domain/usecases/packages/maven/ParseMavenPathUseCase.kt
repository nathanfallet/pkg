package digital.guimauve.pkg.domain.usecases.packages.maven

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.models.packages.maven.MavenPath
import io.ktor.http.*

class ParseMavenPathUseCase : IParseMavenPathUseCase {

    override fun invoke(input: List<String>): MavenPath {
        if (input.size < 3) throw ControllerException(HttpStatusCode.NotFound, "invalid_path")
        val filename = input.last()

        val possibleVersion = input[input.size - 2]
        val possibleArtifactId = input[input.size - 3]
        val remainingGroupParts = input.dropLast(3)

        val isProbablyVersion = isVersion(possibleVersion, filename)

        return if (isProbablyVersion) {
            // /group/artifact/version/filename
            val groupId = remainingGroupParts.joinToString(".")
            MavenPath(groupId, possibleArtifactId, possibleVersion, filename)
        } else {
            // /group/artifact/filename (no version)
            val artifactId = possibleVersion
            val groupId = input.dropLast(2).joinToString(".")
            MavenPath(groupId, artifactId, null, filename)
        }
    }

    private fun isVersion(versionSegment: String, filename: String): Boolean {
        // Heuristic: version should be in filename OR be a valid semver
        return filename.contains("-$versionSegment") ||
                Regex("""\d+\.\d+(\.\d+)?([-+].+)?""").matches(versionSegment)
    }

}
