package com.example.features.calculator

data class UserInfoDTO(
    val id: String,
    val age: Int,
    val gender: String,  // "male" или "female"
    val height: Double,  // Рост в см
    val weight: Double,  // Вес в кг
    val goal: String,  // "lose", "maintain", "gain"
    val activityLevel: String  // "low", "medium", "high"
)