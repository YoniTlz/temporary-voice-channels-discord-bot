package space.astrobot.discord.slashcommands.yeniz.vc

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Permit: SlashCommand(
    name = "permit",
    description = "Permet à un utilisateur de rejoindre votre salon vocal",
    parentSlashCommand = Vc(),
    options = listOf(
        OptionData(OptionType.USER, "user", "L'utilisateur à autoriser", true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val user = ctx.getOption<User>(options[0].name)!!

        if(!ctx.guild.isMember(user)) {
            ctx.reply("L'utilisateur n'est pas de ce serveur !")
            return
        }

        ctx.getVoiceChannel().manager.putMemberPermissionOverride(
            user.idLong,
            listOf(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT),
            listOf()
        ).await()

        ctx.reply("<@${user.id}> peut maintenant rejoindre votre salon!")
    }
}
