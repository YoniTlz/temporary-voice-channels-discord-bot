package space.astrobot.discord.events

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient

suspend fun onServerJoin(event: GuildMemberJoinEvent) {
    val userMention = event.user.asMention
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-join"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}

suspend fun onServerLeave(event: GuildMemberRemoveEvent) {
    val userMention = event.user.asMention
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/server-leave"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}

suspend fun onRoleAdded(event: GuildMemberRoleAddEvent) {
    val userMention = event.user.asMention
    val jsonBody = "{" +
            "\"userMention\": \"$userMention\"," +
            "\"roleId\": \"${event.roles[0].id}\"" +
            "}"
    val url = "http://my-webhooks:8080/discord/addRole"
    val res = RestClient.execRequestPost(url, jsonBody.toRequestBody(RestClient.JSON))
    res.close()
}