package io.kay.visuals

import io.kay.DataService.WorkDayModel
import javafx.geometry.Orientation
import javafx.stage.Stage
import tornadofx.*
import java.time.LocalDate

class MainView : View("Log Time") {
    val model: WorkDayModel by inject()

    override val root = form {
        fieldset(labelPosition = Orientation.VERTICAL) {
            field("Date") {
                datepicker(model.day) {
                    value = LocalDate.now()
                    converter = TaimStringConverter()
                }
            }
            listview<Fieldset> {
                items.add(
                    fieldset {
                        field("Start 1") {
                            textfield(model.start1)
                            button("Now")
                        }
                        field("End 1") {
                            textfield(model.end1)
                            button("Now")
                        }
                    }
                )
            }
            button("Save") {
                action {
                    model.commit()
                }
            }
        }
    }
}

class TaimApp : App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minHeight = 400.0
        stage.minWidth = 600.0
        super.start(stage)
    }
}