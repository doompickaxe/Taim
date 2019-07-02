package io.kay.DataService

import io.kay.model.WorkDay
import java.time.LocalDate

class SQLDataService : DataService {
    override fun upsertWorkDay(day: WorkDay) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWorkDay(date: LocalDate): WorkDay? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}