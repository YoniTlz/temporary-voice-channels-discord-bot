package space.astrobot

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RestClient {

    val JSON: MediaType = "application/json; charset=utf-8".toMediaTypeOrNull()!!

    fun execRequestGet(url: String): Response {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Env.WebhookClient.webhook_client_token)
            .build()
        return execRequest(request)
    }

    fun execRequestPost(url: String, body: RequestBody?): Response {
        val request = Request.Builder()
            .method("POST", body)
            .url(url)
            .header("Authorization", Env.WebhookClient.webhook_client_token)
            .build()
        return execRequest(request)
    }
    fun execRequestDelete(url: String, body: RequestBody?): Response {
        val request = Request.Builder()
            .method("DELETE", null)
            .url(url)
            .header("Authorization", Env.WebhookClient.webhook_client_token)
            .build()
        return execRequest(request)
    }

    private fun execRequest(request: Request): Response {
        val (trustAllCerts, sslSocketFactory) = prepareCerts()
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
        return client.newCall(request).execute()
    }

    private fun prepareCerts(): Pair<Array<TrustManager>, SSLSocketFactory> {
        //set self sign certificate
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory
        return Pair(trustAllCerts, sslSocketFactory)
    }
}
