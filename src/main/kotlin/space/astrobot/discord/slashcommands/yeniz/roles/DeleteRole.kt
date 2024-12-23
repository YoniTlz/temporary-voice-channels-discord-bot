package space.astrobot.discord.slashcommands.yeniz.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class DeleteRole : SlashCommand(
    name = "deleterole",
    description = "Supprime un rôle du serveur",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.ROLE, "role", "Le rôle à supprimer", true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val roleId = ctx.getOption<String>(options[0].name)!!
        val roleName = ctx.guild.getRoleById(roleId)?.name

        ctx.reply("<a:loading:1266380721140928512>  Suppression du rôle  **${roleName}** en cours")
        try {
            ctx.guild.getRoleById(roleId)?.delete()?.queue()
            // Reply
            ctx.reply("<:success:1266385899696951419>  Rôle **${roleName}** supprimé avec succès")
        } catch (err: Exception) {
            val payload = "{roleName: $roleName}"
            handleError("DeleteRole", payload, err)
            ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
        }
    }

}


