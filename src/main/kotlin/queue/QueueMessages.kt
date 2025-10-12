package com.example.queue

import kotlinx.serialization.Serializable

/**
 * Сообщение для обработки расчета калькулятора
 */
@Serializable
data class CalculationMessage(
    val userId: String,
    val weight: Double,
    val height: Double,
    val age: Int,
    val gender: String,
    val activityLevel: String,
    val goal: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Сообщение для отправки уведомления
 */
@Serializable
data class NotificationMessage(
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // info, warning, success, error
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Сообщение для отправки email
 */
@Serializable
data class EmailMessage(
    val to: String,
    val subject: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Статистика очередей
 */
@Serializable
data class QueueStats(
    val calculations: Long,
    val notifications: Long,
    val emails: Long,
    val status: String
)

