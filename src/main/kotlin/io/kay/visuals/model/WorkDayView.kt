package io.kay.visuals.model

import javafx.beans.property.Property
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.property
import java.time.LocalDate

class WorkDayView {
    var day by property<LocalDate>()
    fun dayProperty() = getProperty(WorkDayView::day)
}

class WorkDayModel : ItemViewModel<WorkDayView>(WorkDayView()) {
    val day: Property<LocalDate> = bind(autocommit = true) { item?.dayProperty() }
}