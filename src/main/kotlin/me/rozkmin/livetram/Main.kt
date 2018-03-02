package me.rozkmin.livetram

import com.google.gson.Gson
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

fun main(args: Array<String>) {

    val client = Client(NetworkModule())
    client.start()

    embeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>(Netty, 8080) {

        install(DefaultHeaders)
        install(Compression)
        install(CallLogging)
        install(ContentNegotiation)
        routing {
            get("/") {
                call.respondText(mapOf("Status" to "OK").toJson(), ContentType.Application.Json)
            }
            get("/trams") {
                call.respondText(client.provideLatestData().blockingGet(emptyList()).toJson(), ContentType.Application.Json)
            }

            get("/stops") {
                call.respondText(client.provideStops().blockingGet(emptyList()).toJson(), ContentType.Application.Json)
            }
        }
    }.start(wait = true)
}

fun Any.toJson() = Gson().toJson(this)