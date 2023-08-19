package space.astrobot.discord.slashcommands.generator

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import space.astrobot.Bot
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.discord.interactionsLogic.IdManager
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX

class Delete: SlashCommand(
    name = "delete",
    description = "Supprime un générateur",
    parentSlashCommand = Generator()
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        val generators = ctx.guildDto.generators
        val menu = StringSelectMenu(
            customId = IdManager.get(),
            placeholder = "Sélectionnez le générateur à supprimer",
            options = generators.mapIndexed { index, generatorDto ->
                val generatorChannel = ctx.guild.getVoiceChannelById(generatorDto.id)
                SelectOption(
                    label = "${index + 1}) ${generatorChannel?.name ?: "Salon supprimé"}",
                    description = if (generatorChannel?.parentCategory != null) "Depuis la catégorie ${generatorChannel.parentCategory!!.name}" else "Pas dans une catégorie",
                    value = index.toString()
                )
            }
        )

        ctx.reply("Sélectionnez le générateur à supprimer avec le menu ci-dessous\n(Vous avez 60 secondes)", ActionRow.of(menu))

        /*
        Waits for a selection on the menu created above.
        If no selectiong gets made after 60 seconds, the elvis operator will get triggered
        thanks to the withTimeoutOrNull method.
         */
        withTimeoutOrNull(60000) {
            val event = Bot.jda.await<StringSelectInteractionEvent> {
                it.componentId == menu.id
            }

            return@withTimeoutOrNull event.values.first().toIntOrNull()
        }?.let { index ->
            // If the user selected a valid option delete the related generator both from Discord and the database
            val generatorDeleted = generators.removeAt(index)
            ctx.guild.getVoiceChannelById(generatorDeleted.id)?.delete()?.await()
            GuildsDBI.updateValue(ctx.guildId, "generators", generators)
            ctx.reply("Générateur supprimé!")
        } ?: ctx.reply("Action annulée") // Otherwise don't delete anything
    }
}
