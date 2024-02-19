package space.astrobot.discord.slashcommands.yeniz.category

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.discord.events.CHANNEL_REGLEMENT
import space.astrobot.discord.events.MSG_CHOIX_ROLES
import space.astrobot.discord.events.ROLE_GAMER
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import java.util.*
import java.util.function.Consumer

class CreateGamingCategory : SlashCommand(
    name = "create-gaming-category",
    description = "Crée une catégorie (gaming) sur le serveur",
    requiredMemberPermissions = listOf(Permission.MANAGE_ROLES),
    options = listOf(
        OptionData(OptionType.STRING, "nom-categorie", "Le nom de la catégorie", true, false),
        OptionData(OptionType.STRING, "nom-jeu", "Le nom du jeu", true, false),
        OptionData(OptionType.STRING, "rôle", "Le rôle dédié à la catégorie", true, false),
        OptionData(OptionType.STRING, "nom-emoji", "Le nom de l'emoji du rôle", true, false),
        OptionData(OptionType.ATTACHMENT, "icon-emoji", "L'emoji du rôle", true, false)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val categoryName = ctx.getOption<String>(options[0].name)!!
        val gameName = ctx.getOption<String>(options[1].name)!!
        val roleName = ctx.getOption<String>(options[2].name)!!
        val emojiName = ctx.getOption<String>(options[3].name)!!
        val emoji = ctx.getOption<Attachment>(options[4].name)!!
        val emojiIcon = emoji.proxy.downloadAsIcon().await()

        ctx.reply("<a:loading:1206719713191792650>Création de la catégorie  **${categoryName}** en cours")
        try {
            // Create new category
            ctx.guild.createCategory(categoryName)
                .addPermissionOverride(ctx.guild.publicRole, null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(Consumer { category ->
                    initNewCategory(
                        category,
                        roleName,
                        emojiName,
                        emojiIcon,
                        gameName,
                        ctx
                    )
                }) // onSuccess -> initNewCategory
            // Reply
            if (roleName != null) {
                ctx.reply("<a:verifyblue:1142917481976045588> Catégorie **${categoryName}** et rôle **${roleName}** créés avec succès")
            } else {
                ctx.reply("<a:verifyblue:1142917481976045588> Catégorie **${categoryName}** créée avec succès")
            }
        } catch (err: Exception) {
            logErrorOnDiscord(
                "CreateGamingCategory",
                err.message.orEmpty(),
                "{categoryName: $categoryName, gameName: $gameName, roleName: $roleName, emojiName: $emojiName}",
                err.stackTraceToString()
            )
            ctx.reply("❌ㅤOups... Une erreur est survenue")
        }
    }

    private fun initNewCategory(
        category: Category,
        roleName: String,
        emojiName: String,
        emoji: Icon,
        gameName: String,
        ctx: SlashCommandCTX
    ) {
        category.createTextChannel("\uD83D\uDCAC┃\uD835\uDDA2hat")
            .queue(Consumer { channel ->
                run {
                    if (channel != null) {
                        createNewRole(category, roleName, channel, ctx) // Create new dedicated Role
                        createRoleEmoji(category, emojiName, emoji, gameName, ctx) // Create new dedicated Emoji
                    }
                }
            })
    }

    private fun createNewRole(
        category: Category,
        roleName: String?,
        channel: TextChannel,
        ctx: SlashCommandCTX,
    ) {
        if (roleName != null) {
            // Create new dedicated role
            val defaultGameRole = ctx.guild.getRoleById(ROLE_GAMER)!!
            ctx.guild.createCopyOfRole(defaultGameRole).setName(roleName)
                .queue(Consumer { newRole -> allowNewRole(newRole, category, channel) }) // Handle Role permissions
        }
    }

    private fun allowNewRole(
        role: Role,
        category: Category,
        channel: TextChannel,
    ) {
        category.manager.putPermissionOverride(
            role, EnumSet.of(
                Permission.VIEW_CHANNEL,
                Permission.MESSAGE_HISTORY,
                Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK
            ), null
        ).queue(Consumer { t ->
            channel.manager.sync().queue() // Sync permissions with parent category
        })
    }

    private fun createRoleEmoji(
        category: Category,
        emojiName: String,
        emoji: Icon,
        gameName: String,
        ctx: SlashCommandCTX,
    ) {
        ctx.guild.createEmoji(emojiName, emoji, null).queue(Consumer { emoji ->
            val emojiString = "<:${emoji.name}:${emoji.id}>"
            saveNewCategory(category, emojiString, emoji.id, gameName, ctx)
        })
    }

    private fun saveNewCategory(
        category: Category,
        emojiString: String,
        emojiId: String,
        gameName: String,
        ctx: SlashCommandCTX
    ) {
        val url = "http://my-webhooks:8080/discord/categories/add-game"
        val jsonBody = "{" +
                "\"categoryId\": \"${category.id}\"," +
                "\"categoryName\": \"${category.name}\"," +
                "\"gameName\": \"$gameName\"," +
                "\"emojiString\": \"$emojiString\"" +
                "}"
        val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
        res.close()

        if (res.code == 200) {
            // Add reaction
            val channel = ctx.guild.getTextChannelById(CHANNEL_REGLEMENT)!!
            channel.retrieveMessageById(MSG_CHOIX_ROLES).queue(Consumer { message -> message.addReaction(ctx.guild.getEmojiById(emojiId)!!).queue() })
        }
    }
}


