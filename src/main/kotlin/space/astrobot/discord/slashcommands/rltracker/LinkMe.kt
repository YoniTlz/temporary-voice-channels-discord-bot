package space.astrobot.discord.slashcommands.rltracker

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class LinkMe : SlashCommand(
    name = "linkme",
    description = "Associez votre compte pour récupérer vos classement sur Rocket League",
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
        ctx.reply("🌐ㅤAssociation du comte en cours...")

        val plateforme = ctx.getOption<String>(options[0].name)!!
        val identifiant = ctx.getOption<String>(options[1].name)!!
        val userId = ctx.userId
        val url = "http://my-webhooks:8080/rl-tracker/linkme"
        val jsonBody = "{" +
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
                ctx.reply("⚠️ㅤTu a déjà associé un compte")
            }
            else -> {
                val data = res.body?.string()
                val json = JSONObject(data)
                val displayName = json.getString("displayName")
                ctx.reply("✅ㅤLe compte **$plateformeString** - **$displayName** a été correctement associé")
            }
        }
        res.close()
    }
}


