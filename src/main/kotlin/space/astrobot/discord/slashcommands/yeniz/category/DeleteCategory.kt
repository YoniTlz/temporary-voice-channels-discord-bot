package space.astrobot.discord.slashcommands.yeniz.category

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.RestClient.logErrorOnDiscord
import space.astrobot.WebhookClient
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class DeleteCategory : SlashCommand(
    name = "delete-category",
    description = "Supprime une catégorie sur le serveur",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.CHANNEL, "category", "La catégorie où le générateur sera créé", true)
            .setChannelTypes(ChannelType.CATEGORY),
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val categoryId = ctx.getOption<String>(options[0].name)!!
        val categoryName = ctx.guild.getCategoryById(categoryId)?.name

        ctx.reply("<a:loading:1266380721140928512> Suppression de la catégorie  **${categoryName}** en cours")
        try {
            // Delete all category channels
            val category = ctx.guild.categories.find { cat -> cat.name == categoryName }!!
            category.channels.map { channel -> channel.delete().queue() }
            // Delete category
            category.delete().queue()
            // Delete dedicated role
            val categoryInfo = WebhookClient.getCategoryById(categoryId)!!
            val roleId = categoryInfo["roleId"] as String
            val role = ctx.guild.getRoleById(roleId)!!
            role.delete().queue()

            // Reply
            ctx.reply("🗑️ Catégorie **${categoryName}** suppriméée avec succès")
        } catch (err: Exception) {
            val payload = "{categoryName: $categoryName}"
            handleError("DeleteCategory", payload, err)
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }
}


