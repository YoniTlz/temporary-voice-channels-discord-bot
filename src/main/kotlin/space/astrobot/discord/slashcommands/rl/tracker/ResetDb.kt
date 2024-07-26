package space.astrobot.discord.slashcommands.rl.tracker

import net.dv8tion.jda.api.Permission
import space.astrobot.RestClient
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class ResetDb : SlashCommand(
    name = "resetdb",
    description = "Vider la base de données du BOT RL-Tracker",
    requiredMemberPermissions = listOf(Permission.MANAGE_PERMISSIONS)
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        try {
            val res = RestClient.execRequestPost("http://my-webhooks:8080/rl-tracker/resetCollection", null)
            if (res.code == 500) {
                ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
            } else {
                ctx.reply("<:success:1266385899696951419>ㅤLa base de données a correctement été réinitialisée")
            }
            res.close()
        } catch (err: Exception) {
            val payload = "N/A"
            handleError("ResetDb", payload, err)
            ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
        }
    }
}


