package io.kay.dataService

import io.kay.model.Part
import io.kay.model.WorkDay
import io.kay.visuals.PartModel
import io.kay.visuals.PartView
import io.kay.visuals.WorkDayModel
import javafx.beans.property.Property
import java.lang.RuntimeException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

val timePattern = DateTimeFormatter.ofPattern("HH:mm")

class ModelViewTransformer {

    companion object {
        fun workDayFromView(view: WorkDayModel, parts: List<PartModel>) =
            WorkDay(view.day.value, parts.map { partFromView(it) })

        fun viewFromPart(part: Part): PartModel =
            with(PartView()) {
                start = part.start?.format(timePattern)
                end = part.end?.format(timePattern)
                id = part.id.toString()
                PartModel(this)
            }

        private fun partFromView(view: PartModel): Part {
            return Part(
                parseOrThrow(view.start),
                if (view.end.value == null) null else parseOrThrow(view.end),
                if (view.id.value == null) null else UUID.fromString(view.id.value)
            )
        }

        private fun parseOrThrow(property: Property<String>): LocalTime {
            return try {
                LocalTime.parse(property.value)
            } catch (e: DateTimeParseException) {
                throw RuntimeException(e)
            }
        }
    }
}
