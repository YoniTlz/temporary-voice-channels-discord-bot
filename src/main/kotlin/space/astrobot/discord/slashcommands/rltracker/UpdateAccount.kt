package space.astrobot.discord.slashcommands.rltracker

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class UpdateAccount : SlashCommand(
    name = "update-account",
    description = "Ré-associer son compte Rocket League",
    options = listOf(
        OptionData(OptionType.STRING, "plateforme", "Plateforme du compte", true)
            .addChoices(
                Command.Choice("Epic Games", "epic"),
                Command.Choice("Steam", "steam"),
                Command.Choice("Playstation", "psn"),
                Command.Choice("Xbox", "xbox"),
                Command.Choice("Nintendo Switch", "switch")
            ),
        OptionData(OptionType.STRING, "identifiant", "Identifiant du compte", true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        ctx.reply("🌐ㅤMise à jour du comte en cours...")
        try {
            val channelId = ctx.channel.id
            val plateforme = ctx.getOption<String>(options[0].name)!!
            val identifiant = ctx.getOption<String>(options[1].name)!!
            val userId = ctx.userId
            val url = "http://my-webhooks:8080/rl-tracker/update-profile"
            val jsonBody = "{" +
                    "\"channelId\": \"$channelId\"," +
                    "\"userId\": \"$userId\"," +
                    "\"platformId\": \"$identifiant\"," +
                    "\"platform\": \"$plateforme\"" +
                    "}"
            val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
            val plateformeString = when (plateforme) {
                "epic" -> "Epic Games"
                else -> plateforme.replaceFirstChar(Char::titlecase)
            }
            when (res.code) {
                404 -> {
                    val username = identifiant.replaceFirstChar(Char::titlecase)
                    ctx.reply("❌️ㅤLe compte **$username** est introuvable sur la plateforme **$plateformeString**")
                }

                422 -> {
                    ctx.reply("⚠️ㅤTu a déjà associé ce compte")
                }

                else -> {
                    ctx.reply("✅  Ton compte **$plateformeString** a été correctement mis à jour")
                }
            }
            res.close()
        } catch (err: Exception) {
            println("Une erreur est survenue: ${err}")
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }
}


