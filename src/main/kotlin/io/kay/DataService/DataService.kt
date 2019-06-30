package io.kay.DataService

import java.time.LocalDate

interface DataService {
    fun upsertWorkDay(day: WorkDay)
    fun getWorkDay(date: LocalDate): WorkDay?
}