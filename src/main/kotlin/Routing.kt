package trab

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import trab.casino.ExchangeLogic
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import kotlinx.serialization.Serializable
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun Application.configureRouting() {
    val exchangeLogic = ExchangeLogic() // Create an instance of ExchangeLogic

    routing {
        get("/") {
            call.respondRedirect("/static/index.html")
        }

        staticResources("/static", "static")

        post("/exchange-money-for-chips") {
            val player = call.sessions.get<Player>()
            val moneyToExchange = call.receiveParameters()["money"]?.toIntOrNull() // Parse as Int
            if (player != null && moneyToExchange != null) {
                val updatedPlayer = exchangeLogic.exchangeMoneyForChips(player, moneyToExchange)
                if (updatedPlayer != null) {
                    call.sessions.set(updatedPlayer)
                    call.respondRedirect("/receptionist")
                } else {
                    call.respondText("Invalid exchange request", status = HttpStatusCode.BadRequest)
                }
            } else {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
            }
        }

        post("/exchange-chips-for-money") {
            val player = call.sessions.get<Player>()
            val chipsToExchange = call.receiveParameters()["chips"]?.toIntOrNull()
            if (player != null && chipsToExchange != null) {
                val updatedPlayer = exchangeLogic.exchangeChipsForMoney(player, chipsToExchange)
                if (updatedPlayer != null) {
                    call.sessions.set(updatedPlayer)
                    call.respondRedirect("/receptionist")
                } else {
                    call.respondText("Invalid exchange request", status = HttpStatusCode.BadRequest)
                }
            } else {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
            }
        }
    }
}