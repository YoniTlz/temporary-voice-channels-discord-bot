package space.astrobot.discord.events

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import java.util.stream.Collectors

const val MSG_CHOIX_ROLES: String = "1148294440952287343"
const val ROLE_GAMER: String = "1113468877381304450"
const val ROLE_ROCKETTEUR: String = "1068560168863928330"
const val ROLE_APEXEUR: String = "1068560673384173698"

suspend fun onServerJoin(event: GuildMemberJoinEvent) {
    val userMention = event.user.asMention
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-join"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
    event.jda.getRoleById(ROLE_GAMER)?.let { event.guild.addRoleToMember(event.member, it) }
}

suspend fun onServerLeave(event: GuildMemberRemoveEvent) {
    val userName = event.user.effectiveName
    val jsonBody = "{" +
            "\"userName\": \"$userName\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-leave"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}

suspend fun onAddMessageReaction(event: MessageReactionAddEvent) {
    val msgId = event.messageId
    val userMention = event.member?.asMention
    var roleId = ""

    if (msgId == MSG_CHOIX_ROLES) {
        val emojiId = event.emoji.asCustom().id
        if (emojiId == "1082687683773616239") {
            roleId = ROLE_ROCKETTEUR
        } else if (emojiId == "1082692441104199741") {
            roleId = ROLE_APEXEUR
        }
    }

    val alreadyHasRole =
        event.member?.roles?.stream()?.map { it.id }?.collect(Collectors.toList())?.contains(roleId)!!

    if (userMention != null && roleId != null && !alreadyHasRole) {
        event.guild.getRoleById(roleId)?.let { event.guild.addRoleToMember(event.member!!, it).queue() };
        welcomeRole(userMention, roleId)
    }
}

suspend fun welcomeRole(userMention: String, roleId: String) {
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"," +
            "\"roleId\": \"$roleId\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/addRole"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}

suspend fun onCommandAutocomplete(event: CommandAutoCompleteInteractionEvent) {
    var suggestions: List<Choice> = mutableListOf()

    if (event.fullCommandName == "addrole"|| event.fullCommandName == "removerole") {
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