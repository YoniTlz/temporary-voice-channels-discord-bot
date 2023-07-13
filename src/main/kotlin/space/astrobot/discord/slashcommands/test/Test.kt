package space.astrobot.discord.slashcommands.test

import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Test : SlashCommand(
    name = "test",
    description = "Commande de test",
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        // Do something
    }
}


