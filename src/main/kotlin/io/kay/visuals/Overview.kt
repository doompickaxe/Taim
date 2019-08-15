package io.kay.visuals

import io.kay.dataService.TaimaDataService
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import tornadofx.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun EventTarget.overview(dataService: TaimaDataService): Form {
    val conditions = dataService.fetchConditions(LocalDate.now())
    val fromProp = SimpleObjectProperty<LocalDate>()
    val toProp = SimpleObjectProperty<LocalDate>()


    return form {
        hbox {
            fieldset(text = "Information") {
                field("VacationLeft: ") {
                    label(conditions.vacationLeft.toString())
                }
                field("To work on monday: ") {
                    label(conditions.monday.format())
                }
                field("To work on tuesday: ") {
                    label(conditions.tuesday.format())
                }
                field("To work on wednesday: ") {
                    label(conditions.wednesday.format())
                }
                field("To work on thursday: ") {
                    label(conditions.thursday.format())
                }
                field("To work on friday: ") {
                    label(conditions.friday.format())
                }
                field("To work on saturday: ") {
                    label(conditions.saturday.format())
                }
                field("To work on sunday: ") {
                    label(conditions.sunday.format())
                }
            }

            fieldset(text = "Report") {
                field("from") {
                    datepicker(fromProp)
                }
                field("to") {
                    datepicker(toProp)
                }

                button("Download report CSV") {
                    action {
                        dataService.downloadReport(fromProp.value, toProp.value)
                    }
                    enableWhen(fromProp.isNotNull.and(toProp.isNotNull))
                }
            }
        }
    }
}

private fun LocalTime.format() = this.format(DateTimeFormatter.ofPattern("HH:mm"))