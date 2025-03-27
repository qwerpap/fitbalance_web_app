package com.example.features.calculator

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDTO(
    val id: String,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val goal: String,
    val activityLevel: String
)