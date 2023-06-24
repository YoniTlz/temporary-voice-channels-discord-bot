package space.astrobot.discord.slashcommands.epicgames

import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommand
import space.astrobot.discord.interactionsLogic.slashcommands.SlashCommandCTX
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class FreeGames: SlashCommand(
    name = "free-games",
    //description = "Gets the list of weekly Epic Games Store free games",
    description = "Récupère la liste des jeux de la semaine gratuits sur Epic Games Store",
    parentSlashCommand = Epic(),
) {
    override suspend fun execute(ctx: SlashCommandCTX) {
        println("////ici")
//        val client = HttpClient.newBuilder().build();
//        val request = HttpRequest.newBuilder()
//            .uri(URI.create("http://yonitlz.synology.me/epic-free-games?isDev=true"))
//            .build();
//        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        println(response.body())
    }
}
