package cc.woverflow.onecore.utils.http

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.utils.http.HttpRequester
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class HttpRequesterImpl : HttpRequester {
    override val httpClient = OkHttpClient.Builder()
        .addInterceptor { it.proceed(it.request().newBuilder().header("User-Agent", "${OneCore.getName()}/${OneCore.getVersion()} (Mozilla 4.76)").build()) }
        .build()
    override suspend fun request(request: Request, block: (Response) -> Unit) {
        val response = httpClient.newCall(request).execute()
        block.invoke(response)
        response.close()
    }
}