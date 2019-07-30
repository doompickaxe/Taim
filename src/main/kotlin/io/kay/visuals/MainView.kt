package io.kay.visuals

import io.kay.dataService.TaimaDataService
import tornadofx.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainView : View("Log Time") {

    val dataService = TaimaDataService()
    val dayModel: WorkDayModel by inject()

    override val root = drawer {
        item("Log time", expanded = true) {
            logTimeView(dayModel)
        }

        item("Overview") {
            form {
                fieldset {
                    val conditions = dataService.fetchConditions(LocalDate.now())

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
            }
        }
    }
}

private fun LocalTime.format() = this.format(DateTimeFormatter.ofPattern("HH:mm"))