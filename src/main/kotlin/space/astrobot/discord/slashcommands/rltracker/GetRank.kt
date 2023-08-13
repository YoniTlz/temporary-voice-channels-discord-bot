package space.astrobot.discord.slashcommands.rltracker

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class GetRank : SlashCommand(
    name = "getrank",
    description = "Récupérer le classement d'un joueur Rocket League",
    options = listOf(
        OptionData(OptionType.STRING, "plateforme", "Plateforme du compte", true)
            .addChoices(
                Command.Choice("Epic Games", "epic"),
                Command.Choice("Steam", "steam"),
                Command.Choice("Playstation", "psn"),
                Command.Choice("Xbox", "xbox"),
                Command.Choice("Nintendo Switch", "switch")
            ),
        OptionData(OptionType.STRING, "identifiant", "Identifiant du compte", true),
        OptionData(OptionType.STRING, "playlist", "Le type de playlist", true)
            .addChoices(
                Command.Choice("Standard", "standard"),
                Command.Choice("Extra", "extra")
            )
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        ctx.reply("🌐ㅤRécupération des classements. Cela peut prendre quelques secondes...")

        var channelId = ctx.channel.id
        var userId = ctx.userId
        val plateform = ctx.getOption<String>(options[0].name)!!
        var plateformId = ctx.getOption<String>(options[1].name)!!
        val playlist = ctx.getOption<String>(options[2].name)!!

        val jsonBody = "{" +
                "\"channelId\": \"$channelId\"," +
                "\"userId\": \"$userId\"," +
                "\"platform\": \"$plateform\"," +
                "\"platformId\": \"$plateformId\"," +
                "\"playlist\": \"$playlist\"" +
                "}"
        val res = RestClient.execRequestPost(
            "http://my-webhooks:8080/rl-tracker/getrank",
            jsonBody.toRequestBody(RestClient.JSON)
        )
        println("$$$ $res")
        if (res.code == 404) {
            ctx.reply("❌ㅤCompte introuvable - Vérifie que la plateforme et l'identifiant sont corrects")
        } else {
            val data = res.body?.string()
            val json = JSONObject(data)
            val displayName = json.getString("displayName")
            ctx.reply("✅ㅤLes classements de ***${displayName}*** ont été correctement récupérés")
        }
        res.close()
    }
}

