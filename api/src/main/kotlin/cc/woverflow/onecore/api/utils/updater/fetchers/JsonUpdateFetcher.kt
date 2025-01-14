package cc.woverflow.onecore.api.utils.updater.fetchers

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.utils.updater.UpdateFetcher
import cc.woverflow.onecore.api.utils.updater.Updater
import cc.woverflow.onecore.api.utils.updater.UpdaterMod
import okhttp3.Request

class JsonUpdateFetcher(
    val versionFieldName: String,
    val checksumFieldName: String? = null
) : UpdateFetcher {
    // TODO 2022/02/13 - THIS IS NOT VERY STABLE OR RELIABLE.
    private var hasUpdate = false

    override fun check(updater: Updater, mod: UpdaterMod) {
        val request = Request.Builder()
            .build()
        val response = updater.httpClient.newCall(request).execute()
        response.body?.let {
            val raw = OneCore.getJsonHelper().parse(it.string())
            if (!raw.isJsonObject) return@let
            val json = raw.asJsonObject
            if (!json.has(versionFieldName)) return@let
            val version = json.get(versionFieldName).toString()
            if (version != mod.version) hasUpdate = true
        } ?: OneCore.getNotifications().post(OneCore.getName(), "Failed to check for ${mod.name} updates.")
    }

    override fun hasUpdate() = hasUpdate
}