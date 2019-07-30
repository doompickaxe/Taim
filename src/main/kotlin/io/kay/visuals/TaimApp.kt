package io.kay.visuals

import javafx.stage.Stage
import tornadofx.App

class TaimApp : App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minHeight = 500.0
        stage.minWidth = 700.0
        super.start(stage)
    }
}