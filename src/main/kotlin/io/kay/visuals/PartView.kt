package io.kay.visuals

import javafx.beans.property.Property
import tornadofx.ItemViewModel
import tornadofx.getProperty
import tornadofx.property
import java.time.LocalTime

class PartView {
    var start by property<String>()
    fun startProperty() = getProperty(PartView::start)

    var end by property<String>()
    fun endProperty() = getProperty(PartView::end)
}

class PartModel(partView: PartView = PartView()) : ItemViewModel<PartView>(partView) {
    val start: Property<String> = bind(autocommit = true) { item?.startProperty() }
    val end: Property<String> = bind(autocommit = true) { item?.endProperty() }
}
