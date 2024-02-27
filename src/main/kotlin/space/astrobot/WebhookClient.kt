package space.astrobot

import com.google.gson.Gson
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

object WebhookClient {

    fun getCategoryByEmoji(emojiId: String): Map<*, *>? {
        val queryString = "{\"emojiId\": \"$emojiId\"}"
        return getCategoryByQuery(queryString)
    }

    fun getCategoryById(categoryId: String): Map<*, *>? {
        val queryString = "{\"discordId\": \"$categoryId\"}"
        return getCategoryByQuery(queryString)
    }

    fun getCategoryByQuery(queryString: String): Map<*, *>? {
        val url = "http://my-webhooks:8080/discord/categories/query"
        val jsonBody = "{\"queryString\":$queryString}"
        val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
        if (res.code == 200) {
            val data = res.body?.string()
            val category = JSONArray(data)[0].toString()
            val gson = Gson()
            return gson.fromJson(category, Map::class.java)
        } else {
            throw Exception("Erreur lors de la récupération de la catégorie, query: $queryString")
        }
    }
}
