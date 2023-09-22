package space.astrobot.discord.slashcommands.yeniz.vc

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Limit: SlashCommand(
    name = "limit",
    description = "Définit une limite d'utilisateurs pour votre salon vocal",
    parentSlashCommand = Vc(),
    options = listOf(
        OptionData(OptionType.INTEGER, "limit", "La nouvelle limite", true)
            .setMinValue(0)
            .setMaxValue(99)
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val limit = ctx.getOption<Int>(options[0].name)!!

        ctx.getVoiceChannel().manager.setUserLimit(limit).await()

        ctx.reply("Définit la limite d'utilisateurs de votre salon vocal à `$limit`!")
    }
}
