package space.astrobot.discord.events.rl.tracker

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import space.astrobot.discord.events.onAddMessageReaction

object RlTrackerEventsManager {
    fun manage(jda: JDA) {
        jda.listener<GenericEvent> {
            when (it) {
                is SlashCommandInteractionEvent -> onSlashCommand(it)
                is ButtonInteractionEvent -> onButtonInteraction(it)
            }
        }
    }
}
