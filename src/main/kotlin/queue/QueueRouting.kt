package com.example.queue

import com.example.queueService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Роутинг для работы с очередями
 */
fun Application.configureQueueRouting() {
    val logger = LoggerFactory.getLogger("QueueRouting")
    
    routing {
        route("/queue") {
            // Отправить расчет в очередь для асинхронной обработки
            post("/calculation") {
                try {
                    val message = call.receive<CalculationMessage>()
                    val queueService = call.queueService
                    
                    val success = queueService.sendMessage(QueueService.CALCULATION_QUEUE, message)
                    
                    if (success) {
                        call.respond(
                            HttpStatusCode.Accepted,
                            mapOf(
                                "status" to "queued",
                                "message" to "Calculation queued for processing",
                                "userId" to message.userId
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf("error" to "Failed to queue calculation")
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error queuing calculation: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request: ${e.message}")
                    )
                }
            }
            
            // Отправить уведомление в очередь
            post("/notification") {
                try {
                    val message = call.receive<NotificationMessage>()
                    val queueService = call.queueService
                    
                    val success = queueService.sendMessage(QueueService.NOTIFICATIONS_QUEUE, message)
                    
                    if (success) {
                        call.respond(
                            HttpStatusCode.Accepted,
                            mapOf(
                                "status" to "queued",
                                "message" to "Notification queued",
                                "userId" to message.userId
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf("error" to "Failed to queue notification")
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error queuing notification: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request: ${e.message}")
                    )
                }
            }
            
            // Отправить email в очередь
            post("/email") {
                try {
                    val message = call.receive<EmailMessage>()
                    val queueService = call.queueService
                    
                    val success = queueService.sendMessage(QueueService.EMAIL_QUEUE, message)
                    
                    if (success) {
                        call.respond(
                            HttpStatusCode.Accepted,
                            mapOf(
                                "status" to "queued",
                                "message" to "Email queued for sending",
                                "to" to message.to
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf("error" to "Failed to queue email")
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error queuing email: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request: ${e.message}")
                    )
                }
            }
            
            // Получить статистику очереди
            get("/stats") {
                try {
                    val queueService = call.queueService
                    
                    val stats = QueueStats(
                        calculations = queueService.getMessageCount(QueueService.CALCULATION_QUEUE),
                        notifications = queueService.getMessageCount(QueueService.NOTIFICATIONS_QUEUE),
                        emails = queueService.getMessageCount(QueueService.EMAIL_QUEUE),
                        status = if (queueService.ping()) "connected" else "disconnected"
                    )
                    
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    logger.error("Error getting queue stats: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to get queue stats: ${e.message}")
                    )
                }
            }
            
            // Очистить очередь (только для разработки/тестирования)
            delete("/{queueName}") {
                try {
                    val queueName = call.parameters["queueName"] ?: throw IllegalArgumentException("Missing queue name")
                    val queueService = call.queueService
                    
                    val validQueues = listOf(
                        QueueService.CALCULATION_QUEUE,
                        QueueService.NOTIFICATIONS_QUEUE,
                        QueueService.EMAIL_QUEUE
                    )
                    
                    if (queueName !in validQueues) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid queue name. Valid queues: $validQueues")
                        )
                        return@delete
                    }
                    
                    val success = queueService.purgeQueue(queueName)
                    
                    if (success) {
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf("message" to "Queue '$queueName' purged successfully")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to purge queue")
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Error purging queue: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid request: ${e.message}")
                    )
                }
            }
        }
    }
}

