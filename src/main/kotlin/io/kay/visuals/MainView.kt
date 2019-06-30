package io.kay.visuals

import io.kay.DataService.DataService
import io.kay.DataService.LocalDataService
import io.kay.DataService.ModelViewTransformer
import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import tornadofx.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainView : View("Log Time") {
    val dataService: DataService by lazy { LocalDataService() }
    val dayModel: WorkDayModel by inject()
    var partModelList = mutableListOf(PartModel(), PartModel())
    var listViewItems = FXCollections.observableList(getPartsWithBindings())
    val modelHolder = createHolder()

    fun createHolder(): FlowPane {
        val flowPane = FlowPane(Orientation.VERTICAL)
        flowPane.children.addAll(listViewItems)
        return flowPane
    }

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            hbox {
                field("Date") {
                    datepicker(dayModel.day) {
                        value = LocalDate.now()
                        converter = TaimStringConverter()
                        valueProperty().addListener { _, _, new ->
                            listViewItems.remove(0, listViewItems.size)
                            val newParts = dataService
                                .getWorkDay(new)?.parts?.map { ModelViewTransformer.viewFromPart(it) }

                            partModelList = newParts?.toMutableList() ?: mutableListOf(PartModel(), PartModel())
                            if (partModelList.size < 2)
                                partModelList.add(PartModel())

                            listViewItems.addAll(getPartsWithBindings())
                        }
                    }
                }
            }
            scrollpane(fitToHeight = true, fitToWidth = true) {
                content = modelHolder
            }
            button("Save") {
                action {
                    val workDay = ModelViewTransformer.workDayFromView(
                        dayModel,
                        partModelList.filter { it.start.value != null }
                    )
                    dataService.upsertWorkDay(workDay)
                }

                var chainedValidity = dayModel.valid.and(true)
                for (model in partModelList)
                    chainedValidity = chainedValidity.and(model.valid)
                enableWhen(chainedValidity)
            }
        }

        dayModel.validate(decorateErrors = false)
        partModelList.forEach { it.validate(decorateErrors = false) }
    }

    fun timeValidator(str: String?): ValidationMessage? {
        if (str.isNullOrEmpty())
            return null

        return if ("\\d{2}:\\d{2}".toRegex().matches(str))
            ValidationMessage(null, ValidationSeverity.Success)
        else
            ValidationMessage("Did not match time HH:mm", ValidationSeverity.Error)
    }

    fun getFieldPart(title: String, property: Property<String>): Field {
        val field = Field()
        val label = Label(title)
        val textField = TextField()
        textField.bind(property)
        textField.validator {
            timeValidator(it)
        }
        val button = Button("Now")
        button.action {
            textField.text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        field.add(label)
        field.add(textField)
        field.add(button)

        return field
    }

    fun getPartsWithBindings(): List<Pane> {
        return partModelList
            .mapIndexed { i, model ->
                val container = Fieldset()
                container.add(getFieldPart("Start ${i + 1}", model.start))
                container.add(getFieldPart("End ${i + 1}", model.end))

                model.validate(decorateErrors = false)
                container
            }
    }
}