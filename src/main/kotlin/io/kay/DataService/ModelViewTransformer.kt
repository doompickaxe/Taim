package io.kay.DataService

import io.kay.visuals.PartModel
import io.kay.visuals.PartView
import io.kay.visuals.WorkDayModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val timePattern = DateTimeFormatter.ofPattern("HH:mm")

class ModelViewTransformer {

    companion object {
        fun partFromView(view: PartModel): Part {
            return Part(
                LocalTime.parse(view.start.value),
                if (view.end.value == null) null else LocalTime.parse(view.end.value)
            )
        }

        fun workDayFromView(view: WorkDayModel, parts: List<PartModel>) =
            WorkDay(view.day.value, parts.map { partFromView(it) })

        fun viewFromPart(part: Part): PartModel =
            with(PartView()) {
                start = part.start.format(timePattern)
                end = part.end?.format(timePattern)
                PartModel(this)
            }
    }
}
