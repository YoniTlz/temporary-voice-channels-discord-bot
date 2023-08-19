package space.astrobot.discord.slashcommands.generator

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import space.astrobot.models.GeneratorDto

class Create: SlashCommand(
    name = "create",
    description = "Crée un générateur de salons vocaux temporaires",
    parentSlashCommand = Generator(),
    options = listOf(
        OptionData(OptionType.CHANNEL, "category", "La catégorie où le générateur sera créé")
            .setChannelTypes(ChannelType.CATEGORY),
        OptionData(OptionType.INTEGER, "limit", "Le nombre maximum d'utilisateurs dans un salon temporaire")
    )
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val action = ctx.guild.createVoiceChannel("↪ Créer un salon vocal")

        ctx.getOption<Category>(options[0].name)?.let {
            action.setParent(it)
        }
        ctx.getOption<Int>(options[1].name)?.let {
            action.setUserlimit(it)
        }

        val generator = action.await()

        GuildsDBI.pushValue(ctx.guildId, "generators", GeneratorDto(generator.id))

        ctx.reply("Vous pouvez maintenant créer des salons vocaux temporaires en rejoignant ${generator.asMention}!")
    }
}
