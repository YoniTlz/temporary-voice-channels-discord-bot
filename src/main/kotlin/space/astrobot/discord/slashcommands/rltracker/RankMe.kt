package space.astrobot.discord.slashcommands.rltracker

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class RankMe : SlashCommand(
    name = "rankme",
    description = "Récupérez les classements de votre compte Rocket League",
    options = listOf(
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
        var userId = ctx.user.id
        val playlist = ctx.getOption<String>(options[0].name)!!
        val jsonBody = "{" +
                "\"channelId\": \"$channelId\"," +
                "\"userId\": \"$userId\"," +
                "\"playlist\": \"$playlist\"" +
                "}"
        val res = RestClient.execRequestPost(
            "http://my-webhooks:8080/rl-tracker/rankme",
            jsonBody.toRequestBody(RestClient.JSON)
        )
        if (res.code == 422) {
            ctx.reply("❌ㅤAucun compte trouvé - Utilise la commande **/linkme** pour associer ton compte")
        } else {
            ctx.reply("✅ㅤTes classements ont été correctement récupérés")
        }
        res.close()
    }
}


