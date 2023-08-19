package space.astrobot.discord.slashcommands.vc

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Status: SlashCommand(
    name = "status",
    description = "Ouvre, ferme, masque et démasque votre salon vocal",
    parentSlashCommand = Vc(),
    options = listOf(
        OptionData(OptionType.STRING, "action", "Choisissez le statut du salon vocal", true)
            .addChoices(
                Command.Choice("Ouvert", "open"),
                Command.Choice("Fermé", "close"),
                Command.Choice("Masqué", "hide"),
                Command.Choice("Démasqué", "un-hide")
            )
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val action = ctx.getOption<String>(options[0].name)!!

        var denyPermission = false
        var targetPermission = Permission.VIEW_CHANNEL
        var channelStatus = ""
        when (action) {
            "open" -> {
                targetPermission = Permission.VOICE_CONNECT
                channelStatus = "ouvert"
            }
            "close" -> {
                denyPermission = true
                targetPermission = Permission.VOICE_CONNECT
                channelStatus = "fermé"
            }
            "hide" -> {
                denyPermission = true
                targetPermission = Permission.VIEW_CHANNEL
                channelStatus = "masqué"
            }
            "un-hide" -> {
                targetPermission = Permission.VIEW_CHANNEL
                channelStatus = "démasqué"
            }
        }

        ctx.getVoiceChannel().upsertPermissionOverride(ctx.guild.publicRole).let {
            if (denyPermission)
                it.deny(targetPermission)
            else
                it.grant(targetPermission)
        }.await()

        ctx.reply("Votre salon est maintenant **$channelStatus**!")
    }
}
