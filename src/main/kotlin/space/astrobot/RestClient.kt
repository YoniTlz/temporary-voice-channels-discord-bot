package space.astrobot

import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RestClient {

    fun execRequest(url: String) {
        val (trustAllCerts, sslSocketFactory) = prepareCerts()
        val client = OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
        val request = Request.Builder().url(url)
            .build()
        client.newCall(request).execute()
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
