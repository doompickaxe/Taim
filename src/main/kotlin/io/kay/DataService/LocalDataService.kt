package io.kay.DataService

import io.kay.model.WorkDay
import java.time.LocalDate

class LocalDataService : DataService {
    private val map = mutableMapOf<LocalDate, WorkDay>()

    override fun upsertWorkDay(day: WorkDay) {
        map[day.day] = day
    }

    override fun getWorkDay(date: LocalDate): WorkDay? {
        return map[date]
    }
}