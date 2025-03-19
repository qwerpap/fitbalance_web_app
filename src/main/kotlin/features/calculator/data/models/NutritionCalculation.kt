package com.example.features.calculator.data.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class NutritionCalculation(
    val id: String = UUID.randomUUID().toString(), // Уникальный идентификатор расчета
    val userId: String, // Связь с пользователем
    val tdee: Double, // Total Daily Energy Expenditure
    val protein: Double, // in grams
    val fat: Double, // in grams
    val carbs: Double, // in grams
    val recommendedCalories: Double, // based on goal
    val date: String = LocalDateTime.now().toString() // Дата расчета
)