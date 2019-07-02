package io.kay.model

import java.time.LocalDate

data class WorkDay(val day: LocalDate, val parts: List<Part>)