package space.astrobot.discord.interactionsLogic.slashcommands

import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.reflections.Reflections

val logger = KotlinLogging.logger {}


fun initSlashCommandManager(reflection: Reflections, managerName: String): Set<SlashCommand> {
    var commands = reflection.getSubTypesOf(SlashCommand::class.java)
        .map { it.getDeclaredConstructor().newInstance() }
        .toSet()

    // Check if they all have a valid name and description
    val exceededLimits = commands.filter {
        it.name.isEmpty()
                || it.description.isEmpty()
                || it.name.length > 32
                || it.description.length > 100
    }

    if (exceededLimits.isNotEmpty()) {
        logger.error {
            "$managerName ⎯ One or more commands have invalid name / description: ${
                exceededLimits.joinToString(
                    ", "
                ) { it.path }
            }"
        }
        throw IllegalArgumentException("Invalid slash commands parameters")
    }

    // Check for duplicated slash command paths
    val duplicatePaths = commands.groupingBy { it.path }.eachCount().filter { it.value > 1 }
    if (duplicatePaths.isNotEmpty()) {
        logger.error {
            "$managerName ⎯ Found duplicate command paths: ${
                duplicatePaths.toList().joinToString(", ") { "${it.second} duplicates ${it.first}" }
            }"
        }
        throw IllegalArgumentException("Duplicate slash command names")
    }

    return commands
}

fun addCommands(action: CommandListUpdateAction, commands: Set<SlashCommand>) {
    action.addCommands(
        // First find all top-level commands
        commands.filter { it.parentSlashCommand == null }.map { slashCommand ->
            val slashCommandData = Commands.slash(slashCommand.name, slashCommand.description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.requiredMemberPermissions))
                .setGuildOnly(true)

            if (slashCommand.options.isNotEmpty())
                slashCommandData.addOptions(slashCommand.options)
            else {
                // Then add sub commands to them
                slashCommandData.addSubcommands(
                    commands.filter { it.parentSlashCommand != null && it.parentSlashCommand::class == slashCommand::class }
                        .map { subCommand ->
                            SubcommandData(
                                subCommand.name,
                                subCommand.description
                            )
                                .addOptions(subCommand.options)
                        }
                )
            }
        }
    ).queue()
}