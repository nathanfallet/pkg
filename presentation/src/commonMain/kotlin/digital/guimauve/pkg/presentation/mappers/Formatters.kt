package digital.guimauve.pkg.presentation.mappers

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formats an instant for the dashboard. Everything is displayed in UTC: the dashboard has no
 * notion of the timezone of whoever is reading it.
 */
internal fun Instant.formatted(): String = toLocalDateTime(TimeZone.UTC).let {
    "${it.date} ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')} UTC"
}

/**
 * Formats a file size for the dashboard.
 */
internal fun Long.formattedSize(): String = when {
    this >= 1024 * 1024 -> "%.1f MB".format(this / (1024.0 * 1024))
    this >= 1024 -> "%.1f KB".format(this / 1024.0)
    else -> "$this B"
}
