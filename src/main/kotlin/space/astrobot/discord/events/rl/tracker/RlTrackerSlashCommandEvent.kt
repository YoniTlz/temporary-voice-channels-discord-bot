package space.astrobot.discord.events.rl.tracker

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import space.astrobot.discord.interactionsLogic.slashcommands.rl.tracker.RlTrackerSlashCommandsManager

suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
    // Do not listen to DMs
    if (!event.isFromGuild)
        return

    /*
    A command which is not present in the code can be still be present on Discord
    if the bot hasn't updated the commands yet
     */
    val slashCommand = RlTrackerSlashCommandsManager.get(event.fullCommandName) ?: run {
        event.reply("Cette commande n'est plus disponible car elle est obsolète.")
            .setEphemeral(true)
            .queue()
        return
    }

    // Gets the guild settings from the database
    val guildDto = GuildsDBI.getOrCreate(event.guild!!.id)

    // Check if the bot has the required permissions
    if (!event.guild!!.selfMember.hasPermission(slashCommand.requiredBotPermissions)) {
        event.reply("J'ai besoin des autorisations suivantes pour pouvoir exécuter cette commande :\n" +
                slashCommand.requiredBotPermissions.joinToString("\n") { it.getName() }
        ).queue()
        return
    }

    val ctx = if (slashCommand.name === "test") {
        // Do something
        event.reply("Ceci est un test")
        SlashCommandCTX(event, guildDto)
    } else
        SlashCommandCTX(event, guildDto)

    slashCommand.execute(ctx)
}

fun onButtonInteraction(event: ButtonInteractionEvent) {
// DO smthg
}
