package space.astrobot.discord.interactionsLogic.slashcommands.rl.tracker

import org.reflections.Reflections
import space.astrobot.Env
import space.astrobot.RlTrackerBot
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.addCommands
import space.astrobot.discord.interactionsLogic.slashcommands.initSlashCommandManager
import space.astrobot.discord.interactionsLogic.slashcommands.logger

object RlTrackerSlashCommandsManager {
    private val name = "RL-Tracker"
    private var commands: Set<SlashCommand> = setOf()
    private var commandsMap: Map<String, SlashCommand> = mapOf()

    init {
        // Read and instantiate all slash commands
        val reflection = Reflections("space.astrobot.discord.slashcommands.rl.tracker")
        commands = initSlashCommandManager(reflection, name)
        commandsMap = commands.associateBy({ it.path }, { it })
        logger.info { "$name ⎯ Found and initialized ${commands.size} slash commands" }
    }

    suspend fun updateOnDiscord() {
        val action = if (Env.Discord.working_guild_id.lowercase() == "any")
            RlTrackerBot.jda.updateCommands()
        else
            RlTrackerBot.jda.getGuildById(Env.Discord.working_guild_id)?.updateCommands() ?: let {
                logger.error { "RL-Tracker ⎯ Working guild ID not valid" }
                throw NoSuchElementException()
            }

        addCommands(action, commands)
        logger.info { "$name ⎯ Published slash commands to Discord" }
    }


    fun get(path: String) = commandsMap[path]
}