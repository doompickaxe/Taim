package io.kay.visuals

import io.kay.dataService.ModelViewTransformer
import io.kay.dataService.TaimaDataService
import io.kay.model.FreePart
import io.kay.model.FreeType
import io.kay.model.FreeTypeConverter
import io.kay.model.Part
import io.kay.visuals.model.PartModel
import io.kay.visuals.model.WorkDayModel
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.*
import javafx.collections.FXCollections.observableList
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import tornadofx.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val dataService = TaimaDataService()
var statusText = SimpleStringProperty("Ready")
var remoteFreePart: FreePart? = null
var remoteParts = listOf<Part>()
var saveButtonEnabler = SimpleBooleanProperty(true)
val freeFromWorkItem = SimpleObjectProperty(FreeType.NONE)

var partModelList = getWorkParts(LocalDate.now())
var listViewItems = observableList(getPartsWithBindings())
val modelHolder = createHolder(listViewItems)
var toWork = dataService.fetchWorkTimeForDay(LocalDate.now())
var sumWork = SimpleDoubleProperty(sumWorkParts())

fun EventTarget.logTimeView(dayModel: WorkDayModel): Form {
    return form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            header(dayModel)

            borderpane {
                center = scrollpane(fitToHeight = true, fitToWidth = true) {
                    content = modelHolder
                }

                right = fieldset {
                    field("Goal: ") {
                        label(
                            Duration.ofHours(toWork.hour.toLong()).plusMinutes(toWork.minute.toLong()).toMinutes().div(
                                60.0
                            ).toString()
                        )
                    }
                    field("Current: ") {
                        label(sumWork)
                    }
                }
            }

            footer(dayModel)
        }

        dayModel.validate(decorateErrors = false)
        partModelList.forEach { it.validate(decorateErrors = false) }
    }
}

private fun updateFreeFromWorkItem(date: LocalDate) {
    remoteFreePart = dataService.getFreePart(date)
    freeFromWorkItem.value = remoteFreePart!!.reason
}

private fun timeValidator(str: String?): ValidationMessage? {
    if (str.isNullOrEmpty())
        return null

    return if ("\\d{2}:\\d{2}".toRegex().matches(str))
        ValidationMessage(null, ValidationSeverity.Success)
    else
        ValidationMessage("Did not match time HH:mm", ValidationSeverity.Error)
}

private fun LocalTime.format() = this.format(DateTimeFormatter.ofPattern("HH:mm"))

private fun getFieldPart(title: String, property: Property<String>): Field {
    val field = Field()
    val label = Label(title)
    val textField = TextField()
    textField.bind(property)
    textField.validator {
        timeValidator(it)
    }
    val button = Button("Now")
    button.action {
        textField.text = LocalTime.now().format()
    }

    field.add(label)
    field.add(textField)
    field.add(button)

    return field
}

private fun sumWorkParts(): Double {
    return remoteParts
        .filter { it.end != null }
        .map { Duration.between(it.start, it.end) }
        .sumBy { it.toMinutes().toInt() }
        .div(60.0)
}

private fun getWorkParts(date: LocalDate): MutableList<PartModel> {
    val newParts = dataService.getWorkDay(date)

    if (newParts == null) {
        updateFreeFromWorkItem(date)
        return mutableListOf(PartModel(), PartModel())
    }
    freeFromWorkItem.value = FreeType.NONE

    remoteParts = newParts.parts
    val parts = remoteParts.map { ModelViewTransformer.viewFromPart(it) }.toMutableList()
    if (parts.size < 1)
        parts.add(PartModel())
    if (parts.size < 2)
        parts.add(PartModel())

    return parts
}

private fun refreshSaveButtonEnabler(dayModel: WorkDayModel): BooleanBinding {
    var enabler = dayModel.valid.and(true)

    for (part in partModelList)
        enabler = enabler.and(part.valid)

    return enabler
}

private fun getPartsWithBindings(): List<Pane> {
    return partModelList
        .mapIndexed { i, model ->
            val container = Fieldset()
            val start = getFieldPart("Start ${i + 1}", model.start)
            val end = getFieldPart("End ${i + 1}", model.end)

            container.add(start)
            container.add(end)

            model.validate(decorateErrors = false)
            container
        }
}

private fun createHolder(listViewItems: ObservableList<Pane>): FlowPane {
    val flowPane = FlowPane(Orientation.VERTICAL)
    flowPane.bindChildren(listViewItems) { it }
    return flowPane
}

private fun EventTarget.header(dayModel: WorkDayModel) =
    field {
        datepicker(dayModel.day) {
            value = LocalDate.now()
            converter = TaimStringConverter()
            valueProperty().addListener { _, _, new ->
                toWork = dataService.fetchWorkTimeForDay(new)
                partModelList = getWorkParts(new)
                refreshList(dayModel)
            }
        }

        button("Add") {
            action {
                partModelList.add(PartModel())
                refreshList(dayModel)
            }
        }

        combobox(freeFromWorkItem, FreeType.values().toList()) {
            converter = FreeTypeConverter()
            freeFromWorkItem.onChange {
                if (it != FreeType.NONE)
                    listViewItems.forEach(Pane::hide)
                else
                    listViewItems.forEach(Pane::show)
            }
            if (freeFromWorkItem.value != FreeType.NONE) {
                listViewItems.forEach(Pane::hide)
            }
        }
    }

private fun EventTarget.footer(dayModel: WorkDayModel) =
    hbox {
        button("Save") {
            //            shortcut("Ctrl+S")
            action {
                statusText.set("Save ...")
                runAsync {
                    val workDay = ModelViewTransformer.workDayFromView(
                        dayModel,
                        partModelList.filterNot {
                            (it.id.value == null && it.start.value == null) ||
                                    (it.id.value == null && it.start.value != null && it.start.value.isEmpty())
                        }
                    )

                    if (freeFromWorkItem.value == FreeType.NONE) {
                        dataService.upsertWorkDay(workDay, remoteFreePart)
                        partModelList = getWorkParts(workDay.day)
                    } else {
                        dataService.enterWorkFreeDay(workDay, freeFromWorkItem.value, remoteFreePart)
                        partModelList = mutableListOf(
                            PartModel(),
                            PartModel()
                        )
                    }
                } ui {
                    statusText.set("Ready")
                }
            }
            enableWhen(saveButtonEnabler)
        }

        field {
            label(statusText)
        }
    }

private fun refreshList(dayModel: WorkDayModel) {
    listViewItems.remove(0, listViewItems.size)
    listViewItems.addAll(getPartsWithBindings())
    sumWork.value = sumWorkParts()
    refreshSaveButtonEnabler(dayModel)
}