package io.kay.visuals

import javafx.util.StringConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaimStringConverter : StringConverter<LocalDate>() {
    override fun toString(date: LocalDate?) =
        date?.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")) ?: "N/A"

    override fun fromString(dateString: String) = LocalDate.parse(dateString)
}