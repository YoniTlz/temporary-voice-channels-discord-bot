package space.astrobot.discord.slashcommands.yeniz.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.discord.events.ROLE_GAMER
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class CreateRole : SlashCommand(
    name = "createrole",
    description = "Crée un rôle sur le serveur",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.STRING, "nom", "Le nom du rôle", true, false)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val roleName = ctx.getOption<String>(options[0].name)!!
        ctx.reply("<a:loading:1266380721140928512>  Création du rôle  **${roleName}** en cours")
        try {
            val defaultGameRole = ctx.guild.roles.filter { role -> role.id == ROLE_GAMER }[0]
            ctx.guild.createCopyOfRole(defaultGameRole).setName(roleName).queue()
            // Reply
            ctx.reply(" <:success:1266385899696951419>  Rôle **${roleName}** créé avec succès")
        } catch (err: Exception) {
            val payload = "{roleName: $roleName}"
            handleError("CreateRole", payload, err)
            ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
        }
    }

}


