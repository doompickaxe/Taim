package io.kay.visuals

import javafx.stage.Stage
import tornadofx.App

class TaimApp : App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minHeight = 400.0
        stage.minWidth = 600.0
        super.start(stage)
    }
}