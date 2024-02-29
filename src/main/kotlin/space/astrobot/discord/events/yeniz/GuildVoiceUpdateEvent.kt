package space.astrobot.discord.events.yeniz

import dev.minn.jda.ktx.coroutines.await
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.ErrorResponse
import space.astrobot.db.interactors.GuildsDBI
import space.astrobot.models.TempVCDto
import space.astrobot.redis.TempVoiceChannelsRI

private val logger = KotlinLogging.logger {}

suspend fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
    if (event.member.user.isBot)
        return

    val guild = event.guild
    val guildId = guild.id
    val member = event.member
    val userId = member.user.id

    val generators = GuildsDBI.getOrCreate(guildId).generators
    if (generators.isEmpty())
        return

    val activeTempVoiceChannels = TempVoiceChannelsRI.getAllFromGuild(guildId)

    val joinedGenerator = generators.firstOrNull { it.id == event.channelJoined?.id }
    val leftTempVoiceChannelIndex = activeTempVoiceChannels.indexOfFirst { it.id == event.channelLeft?.id && it.ownerId == userId }

    if (joinedGenerator != null && leftTempVoiceChannelIndex == -1) {
        if (!guild.selfMember.hasPermission(Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_CHANNEL)) {
            member.user.openPrivateChannel().await()
                .sendMessage(
                    "Je n'ai pas pu créer de canal vocal temporaire pour toi car" +
                            "Il me manque les autorisations \"Gérer les salons\" ou \"Déplacer les membres\" sur le serveur `${guild.name}`." +
                            "\n\nMerci de le signaler à un administrateur du serveur !"
                ).await()
            return
        }

        val action = guild
            .createVoiceChannel("Vocal de " + event.member.effectiveName)
            .addMemberPermissionOverride(
                guild.selfMember.idLong,
                listOf(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT),
                listOf()
            )

        (event.channelJoined!! as VoiceChannel).parentCategory?.let {
            action.setParent(it)
        }
        (event.channelJoined!! as VoiceChannel).userLimit?.let {
            action.setUserlimit(it)
        }

        try {
            val tempVoiceChannel = action.await()
            guild.moveVoiceMember(member, tempVoiceChannel).await()

            activeTempVoiceChannels.add(
                TempVCDto(
                    tempVoiceChannel.id,
                    userId
                )

            )
            TempVoiceChannelsRI.updateAllForGuild(guildId, activeTempVoiceChannels)
        } catch (e: Exception) {
            logger.error(e) { "Couldn't create a temporary voice channel in guild $guildId" }
        }
    }

    if (leftTempVoiceChannelIndex != -1) {
        if (event.channelLeft!!.members.none { !it.user.isBot }) {
            try {
                event.channelLeft!!.delete().await()
            } catch (e: Exception) {
                if (e is ErrorResponseException && e.errorResponse == ErrorResponse.MISSING_PERMISSIONS || e is InsufficientPermissionException) {
                    member.user.openPrivateChannel().await()
                        .sendMessage(
                            "Je n'ai pas pu supprimer le salon vocal temporaire car" +
                                    "Il me manque les autorisations \"Gérer les salons\" sur le serveur `${guild.name}`." +
                                    "\n\nMerci de le signaler à un administrateur du serveur !"
                        ).await()
                } else
                    logger.error(e) { "Couldn't delete a temporary voice channel (${event.channelLeft!!.id} in guild $guildId" }
            }

            activeTempVoiceChannels.removeAt(leftTempVoiceChannelIndex)
            TempVoiceChannelsRI.updateAllForGuild(guildId, activeTempVoiceChannels)
        } else {
            val newOwner = event.channelLeft!!.members.first { !it.user.isBot }
            activeTempVoiceChannels[leftTempVoiceChannelIndex].ownerId = newOwner.id
            event.guild.getVoiceChannelById(activeTempVoiceChannels[leftTempVoiceChannelIndex].id)!!.manager.setName(newOwner.effectiveName)
            TempVoiceChannelsRI.updateAllForGuild(guildId, activeTempVoiceChannels)
        }
    }
}
