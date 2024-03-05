package space.astrobot.discord.events

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.WebhookClient.getCategoryByEmoji
import java.util.stream.Collectors

const val CHANNEL_REGLEMENT: String = "1064951736755818567"
const val MSG_CHOIX_ROLES: String = "1148294440952287343"
const val ROLE_GAMER: String = "1113468877381304450"

fun onServerJoin(event: GuildMemberJoinEvent) {
    val userMention = event.user.asMention
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-join"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
    event.jda.getRoleById(ROLE_GAMER)?.let { event.guild.addRoleToMember(event.member, it).queue() }
}

fun onServerLeave(event: GuildMemberRemoveEvent) {
    val userName = event.user.effectiveName
    val jsonBody = "{" +
            "\"userName\": \"$userName\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-leave"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()

}

fun onAddMessageReaction(event: MessageReactionAddEvent) {
    val msgId = event.messageId
    val userMention = event.member?.asMention
    var roleId = ""

    // Gestion des rôles
    if (msgId == MSG_CHOIX_ROLES) {
        val emojiId = event.emoji.asCustom().id
        val categoryInfo = getCategoryByEmoji(emojiId)!!
        roleId = categoryInfo["roleId"] as String

        val alreadyHasRole =
            event.member?.roles?.stream()?.map { it.id }?.collect(Collectors.toList())?.contains(roleId)!!

        if (userMention != null && roleId != null && !alreadyHasRole) {
            event.guild.getRoleById(roleId)?.let { event.guild.addRoleToMember(event.member!!, it).queue() };
            welcomeRole(userMention, roleId)
        }
    }
}

fun welcomeRole(userMention: String, roleId: String) {
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"," +
            "\"roleId\": \"$roleId\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/welcome-role"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}

fun onCommandAutocomplete(event: CommandAutoCompleteInteractionEvent) {
    var suggestions: List<Choice> = mutableListOf()
    if (event.fullCommandName == "addrole" || event.fullCommandName == "removerole") {
        val typing: String = event.focusedOption.value.orEmpty()
        if (event.focusedOption.name == "user") {
            val listMember = event.guild?.loadMembers()?.get().orEmpty()
                .filter { member -> member.effectiveName.contains(typing, true) }
            listMember.take(25).forEach { member -> suggestions += Choice(member.effectiveName, member.id) }
        } else if (event.focusedOption.name == "role") {
            val listRoles = event.guild?.roles.orEmpty().filter { role -> role.name.contains(typing, true) }
            listRoles.take(25).forEach { role -> suggestions += Choice(role.name, role.id) }
        }
        return event.replyChoices(suggestions).queue()
    }
    return event.replyChoices(emptyList()).queue()
}