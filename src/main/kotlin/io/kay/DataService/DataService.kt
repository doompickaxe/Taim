package io.kay.DataService

import io.kay.model.WorkDay
import java.time.LocalDate

interface DataService {
    fun upsertWorkDay(day: WorkDay)
    fun getWorkDay(date: LocalDate): WorkDay?
}