package space.astrobot.discord.slashcommands.yeniz.category

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.RestClient.logErrorOnDiscord
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
        OptionData(OptionType.ATTACHMENT, "icon-emoji", "L'emoji du rôle", true, false),
        OptionData(
            OptionType.STRING,
            "welcome-message",
            "Le message d'accueil pour un nouveau membre de la catégorie",
            false,
            false
        )
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val categoryName = ctx.getOption<String>(options[0].name)!!
        val gameName = ctx.getOption<String>(options[1].name)!!
        val roleName = ctx.getOption<String>(options[2].name)!!
        val emojiName = ctx.getOption<String>(options[3].name)!!
        val emoji = ctx.getOption<Attachment>(options[4].name)!!
        val welcomeMessage = ctx.getOption<String>(options[5].name) ?: ""
        val emojiIcon = emoji.proxy.downloadAsIcon().await()

        ctx.reply("<a:loading:1266380721140928512>  Création de la catégorie  **${categoryName}** en cours")
        try {
            // Create new category
            ctx.guild.createCategory(categoryName)
                .addPermissionOverride(ctx.guild.publicRole, null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(Consumer { category ->
                    // onSuccess -> initNewCategory
                    initNewCategory(
                        category,
                        roleName,
                        emojiName,
                        emojiIcon,
                        gameName,
                        welcomeMessage,
                        ctx
                    )
                })
            // Reply
            if (roleName != null) {
                ctx.reply("<:success:1266385899696951419>  Catégorie **${categoryName}** et rôle **${roleName}** créés avec succès")
            } else {
                ctx.reply("<:success:1266385899696951419>  Catégorie **${categoryName}** créée avec succès")
            }
        } catch (err: Exception) {
            val payload =
                "{categoryName: $categoryName, gameName: $gameName, roleName: $roleName, emojiName: $emojiName}"
            handleError("CreateGamingCategory", payload, err)
            ctx.reply("<:error:1266386370947973150>ㅤOups... Une erreur est survenue")
        }
    }

    private fun initNewCategory(
        category: Category,
        roleName: String,
        emojiName: String,
        emoji: Icon,
        gameName: String,
        welcomeMessage: String,
        ctx: SlashCommandCTX
    ) {
        // Create Chat text channel inside category
        category.createTextChannel("\uD83D\uDCAC┃\uD835\uDDA2hat")
            .queue(Consumer { channel ->
                // onSuccess -> Create new dedicated Role
                createNewRole(
                    category,
                    roleName,
                    channel,
                    emojiName,
                    emoji,
                    gameName,
                    welcomeMessage,
                    ctx
                )
            })
    }

    private fun createNewRole(
        category: Category,
        roleName: String?,
        channel: TextChannel,
        emojiName: String,
        emoji: Icon,
        gameName: String,
        welcomeMessage: String,
        ctx: SlashCommandCTX
    ) {
        if (roleName != null) {
            // Create new dedicated role
            val defaultGameRole = ctx.guild.getRoleById(ROLE_GAMER)!!
            ctx.guild.createCopyOfRole(defaultGameRole).setName(roleName)
                .queue(Consumer { newRole ->
                    // onSuccess -> Handle Role permissions & Create new dedicated Emoji})
                    allowNewRole(newRole, category, channel)
                    createRoleEmoji(
                        category,
                        channel.id,
                        roleName,
                        emojiName,
                        emoji,
                        gameName,
                        welcomeMessage,
                        ctx
                    )
                })
        }
    }

    private fun allowNewRole(
        role: Role,
        category: Category,
        channel: TextChannel,
    ) {
        // Stes the new category as private
        category.manager.putPermissionOverride(
            role, EnumSet.of(
                Permission.VIEW_CHANNEL,
                Permission.MESSAGE_HISTORY,
                Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK
            ), null
        ).queue(Consumer { t ->
            // onSuccess -> Sync permissions with parent category
            channel.manager.sync().queue()
        })
    }

    private fun createRoleEmoji(
        category: Category,
        chatChannelId: String,
        roleName: String,
        emojiName: String,
        emoji: Icon,
        gameName: String,
        welcomeMessage: String,
        ctx: SlashCommandCTX,
    ) {
        val roleId = ctx.guild.roles.find { role -> role.name == roleName }!!.id
        ctx.guild.createEmoji(emojiName, emoji, null)
            .queue(Consumer { emoji ->
                // onSuccess -> Save new category in database
                val emojiString = "<:${emoji.name}:${emoji.id}>"
                saveNewCategory(category, chatChannelId, roleId, emojiString, emoji, gameName, welcomeMessage, ctx)
            })
    }

    private fun saveNewCategory(
        category: Category,
        chatChannelId: String,
        roleId: String,
        emojiString: String,
        emoji: RichCustomEmoji,
        gameName: String,
        welcomeMessage: String,
        ctx: SlashCommandCTX
    ) {
        val url = "http://my-webhooks:8080/discord/categories/add-game"
        val jsonBody = "{" +
                "\"categoryId\": \"${category.id}\"," +
                "\"chatChannelId\": \"${chatChannelId}\"," +
                "\"categoryName\": \"${category.name}\"," +
                "\"gameName\": \"$gameName\"," +
                "\"roleId\": \"$roleId\"," +
                "\"emojiString\": \"$emojiString\"," +
                "\"emojiName\": \"${emoji.name}\"," +
                "\"emojiId\": \"${emoji.id}\"," +
                "\"welcomeMessage\": \"${welcomeMessage}\"" +
                "}"

        val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
        res.close()

        if (res.code == 200) {
            // Add reaction
            val channel = ctx.guild.getTextChannelById(CHANNEL_REGLEMENT)!!
            channel.retrieveMessageById(MSG_CHOIX_ROLES)
                .queue(Consumer { message ->
                    message.addReaction(ctx.guild.getEmojiById(emoji.id)!!).queue()
                })
        } else {
            throw Exception("Une erreur est survenue lors de la sauvegarde de la nouvelle catégorie - Voir logs webhooks")
        }
    }
}


