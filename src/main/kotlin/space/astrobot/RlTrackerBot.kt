package space.astrobot

import dev.minn.jda.ktx.jdabuilder.injectKTX
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.dv8tion.jda.api.entities.Activity.of
import net.dv8tion.jda.api.utils.MemberCachePolicy
import space.astrobot.discord.events.rl.tracker.RlTrackerEventsManager
import space.astrobot.discord.interactionsLogic.slashcommands.rl.tracker.RlTrackerSlashCommandsManager

object RlTrackerBot {
    lateinit var jda: JDA

    suspend fun start() {
        jda = JDABuilder.createLight(Env.Discord.discord_bot_rl_tracker_token)
            .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
            .setActivity(of(ActivityType.fromKey(Env.Discord.rl_tracker_activity_type_key), Env.Discord.rl_tracker_activity))
            .injectKTX() // Injects JDA-KTX library
            .build()
            .awaitReady() // Waits until JDA finishes loading

        RlTrackerEventsManager.manage(jda)

        if (Env.Discord.update_slash_commands)
            RlTrackerSlashCommandsManager.updateOnDiscord()
    }
}
