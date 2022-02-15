package cc.woverflow.onecore.api.utils.http

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

interface HttpRequester {
    val httpClient: OkHttpClient
    suspend fun request(request: Request, block: (Response) -> Unit)
}