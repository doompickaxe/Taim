package io.kay.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalTime
import java.util.*

data class Part(
    @JsonProperty("start") @JsonFormat(pattern = "HH:mm") val start: LocalTime? = null,
    @JsonProperty("end") @JsonFormat(pattern = "HH:mm") val end: LocalTime? = null,
    @JsonProperty("id") val id: UUID? = null
)