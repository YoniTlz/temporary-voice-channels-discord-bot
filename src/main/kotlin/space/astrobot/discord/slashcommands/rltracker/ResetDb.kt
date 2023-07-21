package space.astrobot.discord.slashcommands.rltracker

import net.dv8tion.jda.api.Permission
import space.astrobot.RestClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class ResetDb : SlashCommand(
    name = "resetdb",
    description = "Vide la base de données du BOT RL-Tracker",
    requiredMemberPermissions = listOf(Permission.MANAGE_PERMISSIONS)
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val res = RestClient.execRequestPost("http://my-webhooks:8080/rl-tracker/resetCollection", null)
        if (res.code == 500) {
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        } else {
            ctx.reply("✅ㅤLa base de données a correctement été réinitialisée")
        }
        res.close()
    }
}


