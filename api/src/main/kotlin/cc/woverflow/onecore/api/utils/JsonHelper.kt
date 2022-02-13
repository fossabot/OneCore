package cc.woverflow.onecore.api.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser

class JsonHelper {

    //#if MC<=11202
    val jsonParser = JsonParser()
    //#else

    fun parse(json: String): JsonElement {
        //#if MC<=11202
        return jsonParser.parse(json)
        //#else
        //$$ JsonParser.parseString(json)
        //#endif
    }
}