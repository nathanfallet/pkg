package digital.guimauve.pkg.domain.usecases.packages.maven

import digital.guimauve.pkg.domain.models.packages.maven.MavenPath

interface ParseMavenPathUseCase {
    operator fun invoke(segments: List<String>): MavenPath
}
