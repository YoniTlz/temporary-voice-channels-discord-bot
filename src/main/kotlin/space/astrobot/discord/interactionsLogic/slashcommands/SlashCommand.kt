package space.astrobot.discord.interactionsLogic.slashcommands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.RequestBody.Companion.toRequestBody
import space.astrobot.RestClient
import space.astrobot.RestClient.execRequestPost
import java.lang.Error
import java.lang.Exception

// Interface made to have a default execute action for all slash commands
interface ExecutableSlashCommand {
    suspend fun execute(ctx: SlashCommandCTX) {
        ctx.event.reply("Commande obsolète ou non implémentée !").queue()
    }
}

abstract class SlashCommand(
    val name: String,
    val description: String,

    val parentSlashCommand: SlashCommand? = null,
    val category: SlashCommandCategory = parentSlashCommand?.category ?: SlashCommandCategory.NONE,

    val options: List<OptionData> = emptyList(),

    // Required permissions are inherited from the parent command if it exists
    val requiredMemberPermissions: List<Permission> = parentSlashCommand?.requiredMemberPermissions ?: emptyList(),
    val requiredBotPermissions: List<Permission> = parentSlashCommand?.requiredBotPermissions ?: emptyList()
) : ExecutableSlashCommand {
    // Compiled path of the slash command (parent name if existing + self name)
    val path = (parentSlashCommand?.name?.plus(" ") ?: "") + name

    fun handleError(origin: String, payload:String, error: Exception){
        println(error)
        RestClient.logErrorOnDiscord(
            origin,
            error.message.orEmpty(),
            payload,
            error.stackTraceToString()
        )
    }
}
