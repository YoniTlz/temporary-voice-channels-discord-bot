package space.astrobot.discord.slashcommands.yeniz.freegames

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class FreeGames : SlashCommand(
    name = "jeux-gratuits",
    description = "Récupérer la liste des jeux gratuits sur le PSN ou Epic Games Store",
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
        try {
            val action = ctx.getOption<String>(options[0].name)!!
            var format = ctx.getOption<String>(options[1].name).toString().replaceFirstChar(Char::titlecase)
            val isDetailed = format == "Long"
            var channelId = ctx.channel.id
            var url = ""
            var plateforme = ""

            when (action) {
                "ps-plus" -> {
                    url = "http://my-webhooks:8080/psn-free-games?channelId=$channelId&isDetailed=$isDetailed"
                    plateforme = "PS Plus"
                }

                "epic-games" -> {
                    url = "http://my-webhooks:8080/epic-free-games?channelId=$channelId&isDetailed=$isDetailed"
                    plateforme = "Epic Games"
                }
            }

            val res = RestClient.execRequestGet(url)

            // Reply
            ctx.reply("Récupération des jeux gratuits - **$plateforme** - Format **$format**")
        } catch (err: Exception) {
            println("Une erreur est survenue: ${err}")
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }

}


