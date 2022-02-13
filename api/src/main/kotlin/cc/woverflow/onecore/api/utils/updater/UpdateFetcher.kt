package cc.woverflow.onecore.api.utils.updater

interface UpdateFetcher {
    fun check()
    fun hasUpdate(): Boolean
}