package space.astrobot.discord.events

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

object EventsManager {
    fun manage(jda: JDA) {
        jda.listener<GenericEvent> {
            when(it) {
                is SlashCommandInteractionEvent -> onSlashCommand(it)
                is GuildVoiceUpdateEvent -> onGuildVoiceUpdate(it)
                is ButtonInteractionEvent -> onButtonInteraction(it)
                is GuildMemberJoinEvent -> onServerJoin(it)
                is GuildMemberRemoveEvent -> onServerLeave(it)
                is MessageReactionAddEvent -> onAddMessageReaction(it)
            }
        }
    }
}
