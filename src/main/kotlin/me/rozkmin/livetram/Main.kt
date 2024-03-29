package me.rozkmin.livetram

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

fun Application.main() {

    val client = Client(NetworkModule())
    client.start()

    embeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>(Netty, port = System.getenv("PORT").toInt()) {
        install(DefaultHeaders)
        install(Compression)
        install(CallLogging)
        install(ContentNegotiation)
        routing {
            get("/") {
                call.respondText(mapOf("Status" to "OK").toJson(), ContentType.Application.Json)
            }
            get("/trams") {
                call.respondText(client.provideLatestData().blockingGet().toJson(), ContentType.Application.Json)
            }

            get("/stops") {
                call.respondText(client.provideStops().blockingGet().toJson(), ContentType.Application.Json)
            }
        }
    }.start(wait = true)
}

fun Any.toJson() = Gson().toJson(this)



