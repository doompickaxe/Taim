package io.kay.DataService

import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.property
import java.time.LocalDate
import java.time.LocalTime

data class WorkDay(val day: LocalDate, val pats: List<Part>)

class WorkDayView {
    var day by property<LocalDate>()
    fun dayProperty() = getProperty(WorkDayView::day)

    var start1 by property<String>()
    fun start1Property() = getProperty(WorkDayView::start1)

    var end1 by property<String>()
    fun end1Property() = getProperty(WorkDayView::end1)
}

class WorkDayModel : ItemViewModel<WorkDayView>(WorkDayView()) {
    val day: Property<LocalDate> = bind { item?.dayProperty() }
    val start1: StringProperty = bind { item?.start1Property() }
    val end1: StringProperty = bind { item?.end1Property() }
}

data class Part(val start: LocalTime, val end: LocalTime?)