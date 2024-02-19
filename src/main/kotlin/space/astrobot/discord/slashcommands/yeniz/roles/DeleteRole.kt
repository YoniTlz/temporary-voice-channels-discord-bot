package space.astrobot.discord.slashcommands.yeniz.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class DeleteRole : SlashCommand(
    name = "deleterole",
    description = "Supprime un rôle du serveur",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.STRING, "nom", "Le nom du rôle", true, true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val roleName = ctx.getOption<String>(options[0].name)!!
        ctx.reply("Suppression du rôle  **${roleName}** en cours <a:loading:1206719713191792650>")
        try {
            ctx.guild.roles.filter { role -> role.name == roleName }[0].delete().queue()
            // Reply
            ctx.reply("Rôle **${roleName}** supprimé avec succès <a:verifyblue:1142917481976045588>")
        } catch (err: Exception) {
            logErrorOnDiscord("DeleteRole", err.message.orEmpty(), "{roleName: $roleName}", err.stackTraceToString())
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }

}


