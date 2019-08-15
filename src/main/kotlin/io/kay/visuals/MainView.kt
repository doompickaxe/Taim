package io.kay.visuals

import io.kay.dataService.TaimaDataService
import io.kay.visuals.model.WorkDayModel
import tornadofx.*

class MainView : View("Log Time") {

    val dataService = TaimaDataService()
    val dayModel: WorkDayModel by inject()

    override val root = drawer {
        item("Log time", expanded = true) {
            logTimeView(dayModel)
        }

        item("Overview") {
            overview(dataService)
        }
    }
}