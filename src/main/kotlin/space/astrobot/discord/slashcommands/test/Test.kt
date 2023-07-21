package space.astrobot.discord.slashcommands.test

import net.dv8tion.jda.api.Permission
import okhttp3.internal.wait
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Test : SlashCommand(
    name = "test",
    description = "Commande de test",
    requiredMemberPermissions = listOf(Permission.ADMINISTRATOR)
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        // Do something
    }
}


