package io.kay.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class FreePart(
    @JsonProperty("reason") val reason: FreeType,
    @JsonProperty("id") val id: UUID? = null
)