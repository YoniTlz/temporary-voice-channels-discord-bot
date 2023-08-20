package space.astrobot.discord.slashcommands.generator

import net.dv8tion.jda.api.Permission
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand

// Example of a top level command
class Generator: SlashCommand(
    name = "generator",
    description = "Commande parente des commandes generator",
    requiredBotPermissions = listOf(Permission.MANAGE_CHANNEL),
    requiredMemberPermissions = listOf(Permission.ADMINISTRATOR)
)
