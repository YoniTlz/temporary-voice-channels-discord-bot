package space.astrobot.discord.slashcommands.epicgames

import net.dv8tion.jda.api.Permission
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand

// Example of a top level command
class Epic: SlashCommand(
    name = "epic",
    description = "Parent command for all epic commands",
    requiredBotPermissions = listOf(Permission.MANAGE_CHANNEL),
    requiredMemberPermissions = listOf(Permission.VIEW_CHANNEL)
)
