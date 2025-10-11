package com.example.features.calculator

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val age: Int,
    val gender: String, // "male" or "female"
    val height: Double, // in cm
    val weight: Double, // in kg
    val goal: String, // "lose", "maintain", "gain"
    val activityLevel: String, // "low", "medium", "high"
    val userId: String? = null // Optional user ID for saving calculations
)
