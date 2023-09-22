package space.astrobot.discord.interactionsLogic.slashcommands.yeniz

import org.reflections.Reflections
import space.astrobot.Env
import space.astrobot.YenizBot
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.addCommands
import space.astrobot.discord.interactionsLogic.slashcommands.initSlashCommandManager
import space.astrobot.discord.interactionsLogic.slashcommands.logger

object YenizSlashCommandsManager {
    private val name = "Y.E.N.I.Z."
    private var commands: Set<SlashCommand> = setOf()
    private var commandsMap: Map<String, SlashCommand> = mapOf()

    init {
        // Read and instantiate all slash commands
        val reflection = Reflections("space.astrobot.discord.slashcommands.yeniz")
        commands = initSlashCommandManager(reflection, name)
        commandsMap = commands.associateBy({ it.path }, { it })
        logger.info { "$name ⎯ Found and initialized ${commands.size} slash commands" }
    }

    suspend fun updateOnDiscord() {
        val action = if (Env.Discord.working_guild_id.lowercase() == "any")
            YenizBot.jda.updateCommands()
        else
            YenizBot.jda.getGuildById(Env.Discord.working_guild_id)?.updateCommands() ?: let {
                logger.error { "Working guild ID not valid" }
                throw NoSuchElementException()
            }

        addCommands(action, commands)
        logger.info { "$name ⎯ Published slash commands to Discord" }
    }

    fun get(path: String) = commandsMap[path]
}