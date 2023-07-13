package space.astrobot.discord.events

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import okhttp3.OkHttpClient
import okhttp3.Request
import space.astrobot.RestClient
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCategory
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandsManager
import space.astrobot.redis.TempVoiceChannelsRI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
    // Do not listen to DMs
    if (!event.isFromGuild)
        return

    /*
    A command which is not present in the code can be still be present on Discord
    if the bot hasn't updated the commands yet
     */
    val slashCommand = SlashCommandsManager.get(event.fullCommandName) ?: run {
        event.reply("This command is not available anymore as it's outdated.")
            .setEphemeral(true)
            .queue()
        return
    }

    // Gets the guild settings from the database
    val guildDto = GuildsDBI.getOrCreate(event.guild!!.id)

    // Check if the bot has the required permissions
    if (!event.guild!!.selfMember.hasPermission(slashCommand.requiredBotPermissions)) {
        event.reply("I need to following permissions to be able to run this command:\n" +
                slashCommand.requiredBotPermissions.joinToString("\n") { it.getName() }
        ).queue()
        return
    }

    val ctx = if (slashCommand.category == SlashCommandCategory.VC) {
        val member = event.member!!
        val activeTempVoiceChannels = TempVoiceChannelsRI.getAllFromGuild(event.guild!!.id)
        val tempVoiceChannelIndex = activeTempVoiceChannels.indexOfFirst { it.id == member.voiceState!!.channel?.id }

        if (tempVoiceChannelIndex == -1) {
            event.reply("You need to be in a temporary voice channel to use this command!")
                .setEphemeral(true)
                .queue()
            return
        }

        if (activeTempVoiceChannels[tempVoiceChannelIndex].ownerId != event.user.id) {
            event.reply(
                "You are not the owner of this temporary voice channel!" +
                        "\nThe owner is <@${activeTempVoiceChannels[tempVoiceChannelIndex].ownerId}>"
            )
                .setEphemeral(true)
                .queue()
        }

        SlashCommandCTX(event, guildDto, activeTempVoiceChannels[tempVoiceChannelIndex])
    } else if (slashCommand.name === "test") {
        // Do something
        event.reply("Ceci est un test")
        SlashCommandCTX(event, guildDto)
    } else
        SlashCommandCTX(event, guildDto)

    slashCommand.execute(ctx)
}

fun onButtonInteraction(event: ButtonInteractionEvent) {
    when (event.componentId) {
        "fg-epic-voirPlus" -> {
            event.reply("Détail des jeux gratuits **Epic Games**").setEphemeral(true).queue()
            execHttpRequest("https://yonitlz.synology.me/epic-free-games?channelId=${event.channel.id}&isDetailed=true")
        }

        "fg-psn-voirPlus" -> {
            event.reply("Détail des jeux gratuits **PS PLus**").setEphemeral(true).queue()
            execHttpRequest("https://yonitlz.synology.me/psn-free-games?channelId=${event.channel.id}&isDetailed=true")
        }
    }
}

private fun execHttpRequest(url: String) {
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
