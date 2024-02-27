package space.astrobot.discord.slashcommands.yeniz.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class RemoveRole : SlashCommand(
    name = "removerole",
    description = "Supprime un rôle à un membre",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.STRING, "user", "L'utilisateur", true, true),
        OptionData(OptionType.STRING, "role", "Le rôle à supprimer", true, true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val userId = ctx.getOption<String>(options[0].name)!!
        val roleId = ctx.getOption<String>(options[1].name)!!
        try {
            val role = ctx.guild.roles.filter { role -> role.id == roleId }[0]!!
            val member = ctx.guild?.loadMembers()?.get()?.filter { member -> member.id == userId }?.get(0)!!
            ctx.guild.removeRoleFromMember(member, role).queue()

            // Reply
            ctx.reply("Suppression du rôle **${role.name}** à l'utilisateur **${member.effectiveName}**")
        } catch (err: Exception) {
            logErrorOnDiscord("RemoveRole", err.message.orEmpty(), "{userId: $userId, roleId: $roleId}", err.stackTraceToString().replace("\n", "\\n"))
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }

}


