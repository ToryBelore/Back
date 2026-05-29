package com.stockmate

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import com.stockmate.plugins.*
import com.stockmate.utils.DatabaseFactory
import com.stockmate.utils.JwtUtils

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    JwtUtils.init(this)
    DatabaseFactory.init(this)
    configureSerialization()
    configureAuth()
    configureCORS()
    configureStatusPages()
    configureRouting()
}
