package io.kay.model

import javafx.util.StringConverter

enum class FreeType(val readable: String) {
    NONE(""),
    VACATION("vacation"),
    ILL("ill"),
    HOLIDAY("holiday"),
    CARE_FREE("care free"),
    COMPENSATORY("compensatory")
}

class FreeTypeConverter : StringConverter<FreeType>() {
    override fun toString(freeType: FreeType) =  freeType.readable

    override fun fromString(readable: String): FreeType? = FreeType.values().find { it.readable == readable }

}