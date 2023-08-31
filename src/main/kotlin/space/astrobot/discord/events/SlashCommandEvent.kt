package space.astrobot.discord.events

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import space.astrobot.RestClient
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCategory
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandsManager
import space.astrobot.redis.TempVoiceChannelsRI

suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
    // Do not listen to DMs
    if (!event.isFromGuild)
        return

    /*
    A command which is not present in the code can be still be present on Discord
    if the bot hasn't updated the commands yet
     */
    val slashCommand = SlashCommandsManager.get(event.fullCommandName) ?: run {
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

    val ctx = if (slashCommand.category == SlashCommandCategory.VC) {
        val member = event.member!!
        val activeTempVoiceChannels = TempVoiceChannelsRI.getAllFromGuild(event.guild!!.id)
        val tempVoiceChannelIndex = activeTempVoiceChannels.indexOfFirst { it.id == member.voiceState!!.channel?.id }

        if (tempVoiceChannelIndex == -1) {
            event.reply("Vous devez être dans un salon vocal temporaire pour utiliser cette commande !")
                .setEphemeral(true)
                .queue()
            return
        }

        if (activeTempVoiceChannels[tempVoiceChannelIndex].ownerId != event.user.id) {
            event.reply(
                "Tu n'es pas le propriétaire de ce salon vocal temporaire !" +
                        "\nLe propriétaire c'est <@${activeTempVoiceChannels[tempVoiceChannelIndex].ownerId}>"
            )
                .setEphemeral(true)
                .queue()
        }

        SlashCommandCTX(event, guildDto, activeTempVoiceChannels[tempVoiceChannelIndex])
    } else if (slashCommand.name === "test") {
        // Do something
        event.reply("Ceci est un test")
        SlashCommandCTX(event, guildDto)
    } else
        SlashCommandCTX(event, guildDto)

    slashCommand.execute(ctx)
}

fun onButtonInteraction(event: ButtonInteractionEvent) {
    when (event.componentId) {
        "fg-epic-voirPlus" -> {
            event.reply("Détail des jeux gratuits **Epic Games**").setEphemeral(true).queue()
            val url ="http://my-webhooks:8080/epic-free-games?channelId=${event.channel.id}&isDetailed=true"
            RestClient.execRequestGet(url)
        }

        "fg-psn-voirPlus" -> {
            event.reply("Détail des jeux gratuits **PS PLus**").setEphemeral(true).queue()
            val url = "http://my-webhooks:8080/psn-free-games?channelId=${event.channel.id}&isDetailed=true"
            RestClient.execRequestGet(url)
        }
    }
}
