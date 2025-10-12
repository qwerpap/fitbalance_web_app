package com.example

import com.example.auth.JwtConfig
import com.example.auth.configureGoogleAuth
import com.example.auth.configureRoleManagement
import com.example.cache.CacheService
import com.example.queue.QueueService
import com.example.queue.QueueConsumer
import com.example.queue.configureQueueRouting
import com.example.features.admin.configureAdminRouting
import com.example.features.calculator.configureCalculatorRouting
import com.example.features.login.configureLoginRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import io.ktor.util.AttributeKey
import org.jetbrains.exposed.sql.Database
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.http.content.*
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import org.slf4j.event.Level
import kotlinx.serialization.json.Json

fun Application.module() {
    // Content Negotiation
    configureSerialization()
    
    // Database Connection
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/fitbalance"
    val dbUser = System.getenv("DB_USER") ?: "danil"
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""
    
    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )
    
    // Redis Cache Connection
    val redisUrl = System.getenv("REDIS_URL") ?: "redis://localhost:6379"
    val cacheService = CacheService(redisUrl)
    
    // Проверка подключения к Redis
    if (cacheService.ping()) {
        log.info("Successfully connected to Redis at $redisUrl")
    } else {
        log.warn("Failed to connect to Redis at $redisUrl. Cache will be disabled.")
    }
    
    // RabbitMQ Queue Connection
    val rabbitHost = System.getenv("RABBITMQ_HOST") ?: "localhost"
    val rabbitPort = System.getenv("RABBITMQ_PORT")?.toIntOrNull() ?: 5672
    val rabbitUser = System.getenv("RABBITMQ_USER") ?: "fitbalance"
    val rabbitPass = System.getenv("RABBITMQ_PASSWORD") ?: "fitbalance"
    val queueService = QueueService(rabbitHost, rabbitPort, rabbitUser, rabbitPass)
    
    // Проверка подключения к RabbitMQ
    if (queueService.connect()) {
        log.info("Successfully connected to RabbitMQ at $rabbitHost:$rabbitPort")
        
        // Запускаем consumers для обработки сообщений из очередей
        val queueConsumer = QueueConsumer(queueService)
        queueConsumer.startConsumers()
        log.info("Queue consumers started successfully")
    } else {
        log.warn("Failed to connect to RabbitMQ at $rabbitHost:$rabbitPort. Queue will be disabled.")
    }
    
    // Сохраняем сервисы в attributes для доступа из роутов
    environment.monitor.subscribe(ApplicationStopped) {
        cacheService.close()
        queueService.close()
        log.info("Redis and RabbitMQ connections closed")
    }
    
    // Делаем сервисы доступными через attributes
    attributes.put(CacheServiceKey, cacheService)
    attributes.put(QueueServiceKey, queueService)
    
    // Call Logging
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            "Request: ${call.request.uri}"
        }
    }
    
    // JWT Authentication
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val role = credential.payload.getClaim("role").asString()
                if (userId.isNotEmpty() && role.isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    
    // HTTP Client для OAuth запросов
    val httpClient = HttpClient(io.ktor.client.engine.cio.CIO) {
        install(ClientContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    
    // Configure Routing
    routing {
        // Статические файлы
        staticResources("/static", "static")
    }
    
    configureRouting()
    configureLoginRouting()
    configureRegisterRouting()
    configureCalculatorRouting()
    configureGoogleAuth(httpClient)
    configureRoleManagement()
    configureAdminRouting()
    configureQueueRouting()
}

// AttributeKey для сервисов
val CacheServiceKey = AttributeKey<CacheService>("CacheService")
val QueueServiceKey = AttributeKey<QueueService>("QueueService")

// Extensions для удобного доступа к сервисам
val ApplicationCall.cacheService: CacheService
    get() = application.attributes[CacheServiceKey]

val ApplicationCall.queueService: QueueService
    get() = application.attributes[QueueServiceKey]

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

