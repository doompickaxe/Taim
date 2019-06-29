package io.kay.DataService

import java.time.LocalDate

interface DataService {
    fun enterStart(day: WorkDay, begin: Part)
    fun enterEnd(day: WorkDay, part: Part)
    fun getDay(date: LocalDate): List<WorkDay>
}