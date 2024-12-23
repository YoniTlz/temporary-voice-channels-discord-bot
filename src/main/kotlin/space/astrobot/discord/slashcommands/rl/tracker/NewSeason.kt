package space.astrobot.discord.slashcommands.rl.tracker

import net.dv8tion.jda.api.Permission
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX


class NewSeason : SlashCommand(
    name = "newseason",
    description = "Prépare la base de données du BOT RL-Tracker lors du changement de saison classée",
    requiredMemberPermissions = listOf(Permission.MANAGE_PERMISSIONS)
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        try {
            val res = RestClient.execRequestPost(
                "http://my-webhooks:8080/rl-tracker/new-season", "{}".toRequestBody(RestClient.JSON)
            )
            if (res.code == 500) {
                ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
            } else {
                ctx.reply("<:success:1266385899696951419>ㅤLa base de données a correctement été mise à jour")
            }
            res.close()
        } catch (err: Exception) {
            val payload = "N/A"
            handleError("NewSeason", payload, err)
            ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
        }
    }
}


