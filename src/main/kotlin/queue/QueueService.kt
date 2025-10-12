package com.example.queue

import com.rabbitmq.client.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeoutException
import com.example.queue.CalculationMessage
import com.example.queue.NotificationMessage
import com.example.queue.EmailMessage

/**
 * Сервис для работы с RabbitMQ очередью
 * Позволяет отправлять и обрабатывать сообщения асинхронно
 */
class QueueService(
    private val host: String = "localhost",
    private val port: Int = 5672,
    private val username: String = "fitbalance",
    private val password: String = "fitbalance"
) {
    internal val logger = LoggerFactory.getLogger(QueueService::class.java)
    internal var connection: Connection? = null
    internal var channel: Channel? = null
    internal val json = Json { ignoreUnknownKeys = true }
    
    // Названия очередей
    companion object {
        const val CALCULATION_QUEUE = "calculations"
        const val NOTIFICATIONS_QUEUE = "notifications"
        const val EMAIL_QUEUE = "emails"
    }
    
    /**
     * Подключение к RabbitMQ
     */
    fun connect(): Boolean {
        return try {
            val factory = ConnectionFactory().apply {
                this.host = this@QueueService.host
                this.port = this@QueueService.port
                this.username = this@QueueService.username
                this.password = this@QueueService.password
                connectionTimeout = 5000
                requestedHeartbeat = 60
            }
            
            connection = factory.newConnection()
            channel = connection?.createChannel()
            
            // Объявляем очереди (создаются если их нет)
            channel?.queueDeclare(CALCULATION_QUEUE, true, false, false, null)
            channel?.queueDeclare(NOTIFICATIONS_QUEUE, true, false, false, null)
            channel?.queueDeclare(EMAIL_QUEUE, true, false, false, null)
            
            logger.info("Successfully connected to RabbitMQ at $host:$port")
            true
        } catch (e: IOException) {
            logger.error("Failed to connect to RabbitMQ: ${e.message}")
            false
        } catch (e: TimeoutException) {
            logger.error("Connection to RabbitMQ timed out: ${e.message}")
            false
        }
    }
    
    /**
     * Проверка подключения
     */
    fun ping(): Boolean {
        return try {
            channel?.isOpen == true && connection?.isOpen == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Отправка сообщения в очередь
     */
    internal inline fun <reified T> sendMessageInternal(queueName: String, message: T): Boolean {
        return try {
            val messageJson = json.encodeToString(message)
            channel?.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                messageJson.toByteArray()
            )
            logger.info("Message sent to queue '$queueName': $messageJson")
            true
        } catch (e: Exception) {
            logger.error("Failed to send message to queue '$queueName': ${e.message}")
            false
        }
    }
    
    /**
     * Получение количества сообщений в очереди
     */
    fun getMessageCount(queueName: String): Long {
        return try {
            channel?.messageCount(queueName) ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get message count for queue '$queueName': ${e.message}")
            0L
        }
    }
    
    /**
     * Начать обработку сообщений из очереди
     * @param queueName название очереди
     * @param handler функция обработки сообщения
     */
    internal inline fun <reified T> startConsumingInternal(
        queueName: String,
        crossinline handler: suspend (T) -> Unit
    ) {
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                try {
                    val messageJson = String(body, Charsets.UTF_8)
                    logger.info("Received message from queue '$queueName': $messageJson")
                    
                    val message = json.decodeFromString<T>(messageJson)
                    
                    // Обрабатываем сообщение асинхронно
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            handler(message)
                            // Подтверждаем успешную обработку
                            channel?.basicAck(envelope.deliveryTag, false)
                            logger.info("Message processed successfully from queue '$queueName'")
                        } catch (e: Exception) {
                            logger.error("Error processing message from queue '$queueName': ${e.message}")
                            // Возвращаем сообщение в очередь при ошибке
                            channel?.basicNack(envelope.deliveryTag, false, true)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error handling delivery from queue '$queueName': ${e.message}")
                    channel?.basicNack(envelope.deliveryTag, false, true)
                }
            }
        }
        
        try {
            // Начинаем слушать очередь
            channel?.basicConsume(queueName, false, consumer)
            logger.info("Started consuming messages from queue '$queueName'")
        } catch (e: Exception) {
            logger.error("Failed to start consuming from queue '$queueName': ${e.message}")
        }
    }
    
    // Public wrappers для inline функций
    fun sendMessage(queueName: String, message: CalculationMessage): Boolean = sendMessageInternal(queueName, message)
    fun sendMessage(queueName: String, message: NotificationMessage): Boolean = sendMessageInternal(queueName, message)
    fun sendMessage(queueName: String, message: EmailMessage): Boolean = sendMessageInternal(queueName, message)
    
    @JvmName("startConsumingCalculation")
    fun startConsuming(queueName: String, handler: suspend (CalculationMessage) -> Unit) = 
        startConsumingInternal(queueName, handler)
    
    @JvmName("startConsumingNotification")
    fun startConsuming(queueName: String, handler: suspend (NotificationMessage) -> Unit) = 
        startConsumingInternal(queueName, handler)
    
    @JvmName("startConsumingEmail")
    fun startConsuming(queueName: String, handler: suspend (EmailMessage) -> Unit) = 
        startConsumingInternal(queueName, handler)
    
    /**
     * Очистить очередь
     */
    fun purgeQueue(queueName: String): Boolean {
        return try {
            channel?.queuePurge(queueName)
            logger.info("Queue '$queueName' purged")
            true
        } catch (e: Exception) {
            logger.error("Failed to purge queue '$queueName': ${e.message}")
            false
        }
    }
    
    /**
     * Закрытие подключения
     */
    fun close() {
        try {
            channel?.close()
            connection?.close()
            logger.info("RabbitMQ connection closed")
        } catch (e: Exception) {
            logger.error("Error closing RabbitMQ connection: ${e.message}")
        }
    }
}

