package space.astrobot

import dev.minn.jda.ktx.jdabuilder.injectKTX
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.dv8tion.jda.api.entities.Activity.of
import net.dv8tion.jda.api.utils.MemberCachePolicy
import space.astrobot.discord.events.yeniz.YenizEventsManager
import space.astrobot.discord.interactionsLogic.slashcommands.yeniz.YenizSlashCommandsManager

object YenizBot {
    lateinit var jda: JDA

    suspend fun start() {
        jda = JDABuilder.createDefault(Env.Discord.discord_bot_yeniz_token)
            .setMemberCachePolicy(MemberCachePolicy.VOICE)
            .setActivity(of(ActivityType.fromKey(Env.Discord.activity_type_key), Env.Discord.activity))
            .injectKTX() // Injects JDA-KTX library
            .build()
            .awaitReady() // Waits until JDA finishes loading

        YenizEventsManager.manage(jda)

        if (Env.Discord.update_slash_commands)
            YenizSlashCommandsManager.updateOnDiscord()
    }
}
