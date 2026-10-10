package digital.guimauve.pkg.domain.usecases.packages.maven

import digital.guimauve.pkg.domain.models.packages.maven.MavenPath

/**
 * Reads a maven repository path, split into its segments: `group/…/artifact/version/filename`, or
 * `group/…/artifact/filename` for a path that carries no version (such as `maven-metadata.xml`). The
 * version segment is told apart by a heuristic: the filename holds it after a dash, or it looks like a
 * version.
 *
 * Throws [digital.guimauve.pkg.domain.exceptions.packages.maven.InvalidMavenPathException] for fewer
 * than three segments, or a segment that is empty, `.`, `..`, or holds a slash, a backslash, a quote
 * or a character below U+0020.
 */
interface ParseMavenPathUseCase {
    operator fun invoke(segments: List<String>): MavenPath
}
