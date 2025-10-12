package com.example.queue

import com.example.features.calculator.CalculatorService
import org.slf4j.LoggerFactory

/**
 * Consumer для обработки сообщений из очередей
 */
class QueueConsumer(private val queueService: QueueService) {
    private val logger = LoggerFactory.getLogger(QueueConsumer::class.java)
    private val calculatorService = CalculatorService()
    
    /**
     * Запустить обработку всех очередей
     */
    fun startConsumers() {
        startCalculationConsumer()
        startNotificationConsumer()
        startEmailConsumer()
    }
    
    /**
     * Обработчик расчетов калькулятора
     */
    private fun startCalculationConsumer() {
        queueService.startConsuming(
            QueueService.CALCULATION_QUEUE
        ) { message: CalculationMessage ->
            logger.info("Processing calculation for user: ${message.userId}")
            
            try {
                // Выполняем расчет
                val result = calculatorService.calculate(
                    weight = message.weight,
                    height = message.height,
                    age = message.age,
                    gender = message.gender,
                    activityLevel = message.activityLevel,
                    goal = message.goal
                )
                
                logger.info("""
                    Calculation completed for user ${message.userId}:
                    - BMI: ${result.bmi}
                    - BMR: ${result.bmr}
                    - TDEE: ${result.tdee}
                    - Recommended Calories: ${result.recommendedCalories}
                """.trimIndent())
                
                // Здесь можно сохранить результат в БД или отправить уведомление
                // Например, отправить уведомление о завершении расчета
                queueService.sendMessage(
                    QueueService.NOTIFICATIONS_QUEUE,
                    NotificationMessage(
                        userId = message.userId,
                        title = "Расчет завершен",
                        message = "Ваш расчет калорий готов! Рекомендуемое количество: ${result.recommendedCalories} ккал/день",
                        type = "success"
                    )
                )
                
            } catch (e: Exception) {
                logger.error("Error processing calculation: ${e.message}", e)
                
                // Отправляем уведомление об ошибке
                queueService.sendMessage(
                    QueueService.NOTIFICATIONS_QUEUE,
                    NotificationMessage(
                        userId = message.userId,
                        title = "Ошибка расчета",
                        message = "Произошла ошибка при выполнении расчета. Попробуйте снова.",
                        type = "error"
                    )
                )
                
                throw e // Re-throw чтобы сообщение вернулось в очередь
            }
        }
        
        logger.info("Calculation consumer started")
    }
    
    /**
     * Обработчик уведомлений
     */
    private fun startNotificationConsumer() {
        queueService.startConsuming(
            QueueService.NOTIFICATIONS_QUEUE
        ) { message: NotificationMessage ->
            logger.info("Processing notification for user: ${message.userId}")
            
            try {
                // Здесь логика отправки уведомления
                // Например, через WebSocket, Push Notification, или сохранение в БД
                logger.info("""
                    Notification sent to user ${message.userId}:
                    Title: ${message.title}
                    Message: ${message.message}
                    Type: ${message.type}
                """.trimIndent())
                
                // Имитация отправки уведомления
                Thread.sleep(100) // Симулируем задержку
                
            } catch (e: Exception) {
                logger.error("Error processing notification: ${e.message}", e)
                throw e
            }
        }
        
        logger.info("Notification consumer started")
    }
    
    /**
     * Обработчик email
     */
    private fun startEmailConsumer() {
        queueService.startConsuming(
            QueueService.EMAIL_QUEUE
        ) { message: EmailMessage ->
            logger.info("Processing email to: ${message.to}")
            
            try {
                // Здесь логика отправки email
                // Например, через SMTP, SendGrid, AWS SES и т.д.
                logger.info("""
                    Email sent:
                    To: ${message.to}
                    Subject: ${message.subject}
                    Body: ${message.body}
                """.trimIndent())
                
                // Имитация отправки email
                Thread.sleep(200) // Симулируем задержку
                
            } catch (e: Exception) {
                logger.error("Error processing email: ${e.message}", e)
                throw e
            }
        }
        
        logger.info("Email consumer started")
    }
}

