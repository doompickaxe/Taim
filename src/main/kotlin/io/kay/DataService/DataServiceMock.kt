package io.kay.DataService

import java.time.LocalDate

class DataServiceMock : DataService {
    override fun enterStart(day: WorkDay, begin: Part) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enterEnd(day: WorkDay, part: Part) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDay(date: LocalDate): List<WorkDay> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}