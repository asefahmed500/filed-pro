package com.example.server

import com.example.server.database.DatabaseFactory
import com.example.server.routes.fieldForceRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    // Initialize Database Factory with configurations
    DatabaseFactory.init(environment.config)

    // Install Content Negotiation with JSON Serializer
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // Install DoubleReceive to allow reading request body multiple times if needed (e.g. for logging or sync validation)
    install(DoubleReceive)

    // Set up Server Routing API
    routing {
        fieldForceRoutes()
    }
}
