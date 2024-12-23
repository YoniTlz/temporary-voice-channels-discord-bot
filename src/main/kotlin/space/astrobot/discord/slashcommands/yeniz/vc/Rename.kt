package space.astrobot.discord.slashcommands.yeniz.vc

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Rename: SlashCommand(
    name = "rename",
    description = "Modifie le nom de votre salon vocal",
    parentSlashCommand = Vc(),
    options = listOf(
        OptionData(OptionType.STRING, "name", "Le nouveau nom du salon vocal", true)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val name = ctx.getOption<String>(options[0].name)!!

        ctx.getVoiceChannel().manager.setName(name.take(100)).await()

        ctx.reply("Le nom de votre salon a été modifié!")
    }
}
