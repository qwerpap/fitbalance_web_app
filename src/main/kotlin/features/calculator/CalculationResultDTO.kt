package com.example.features.calculator

data class CalculationResultDTO(
    val id: String,
    val userId: String,  // Ссылка на пользователя
    val tdee: Double,  // Total Daily Energy Expenditure
    val protein: Double,  // Белки в граммах
    val fat: Double,  // Жиры в граммах
    val carbs: Double,  // Углеводы в граммах
    val recommendedCalories: Double  // Рекомендуемые калории
)