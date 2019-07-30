package io.kay.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class Conditions(
    @JsonProperty("monday") val monday: LocalTime,
    @JsonProperty("tuesday") val tuesday: LocalTime,
    @JsonProperty("wednesday") val wednesday: LocalTime,
    @JsonProperty("thursday") val thursday: LocalTime,
    @JsonProperty("friday") val friday: LocalTime,
    @JsonProperty("saturday") val saturday: LocalTime,
    @JsonProperty("sunday") val sunday: LocalTime,
    @JsonProperty("from") val from: LocalDate,
    @JsonProperty("to") val to: LocalDate,
    @JsonProperty("vacationLeft") val vacationLeft: Int,
    @JsonProperty("id") val id: UUID
)