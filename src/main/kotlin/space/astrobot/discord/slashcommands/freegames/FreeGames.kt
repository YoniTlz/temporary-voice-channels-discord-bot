package space.astrobot.discord.slashcommands.freegames

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.OkHttpClient
import okhttp3.Request
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class FreeGames : SlashCommand(
    name = "jeux-gratuits",
    description = "Commande qui récupère la liste des jeux gratuits par plateforme",
    options = listOf(
        OptionData(OptionType.STRING, "plateforme", "Le nom de la plateforme", true)
            .addChoices(
                Command.Choice("PS Plus", "ps-plus"),
                Command.Choice("Epic Games", "epic-games")
            ),
        OptionData(OptionType.STRING, "format", "Le niveau de détails", true)
            .addChoices(
                Command.Choice("Court", "court"),
                Command.Choice("Long", "long")
            ),
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val action = ctx.getOption<String>(options[0].name)!!
        var format = ctx.getOption<String>(options[1].name).toString().replaceFirstChar(Char::titlecase)
        val isDetailed = format == "Long"
        var channelId = ctx.channel.id
        var url = ""
        var plateforme = ""

        when (action) {
            "ps-plus" -> {
                url = "https://yonitlz.synology.me/ps-plus-monthly-games?channelId=$channelId&isDetailed=$isDetailed"
                plateforme = "PS Plus"
            }

            "epic-games" -> {
                url = "https://yonitlz.synology.me/epic-free-games?channelId=$channelId&isDetailed=$isDetailed"
                plateforme = "Epic Games"
            }
        }

        val (trustAllCerts, sslSocketFactory) = prepareCerts()
        val client = OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
        val request = Request.Builder().url(url)
            .build()
        client.newCall(request).execute()

        ctx.reply("Récupération des jeux gratuits - **$plateforme** - Format **$format**")
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


