package com.example.features.calculator
import com.example.features.calculator.CalculationResult

class CalculatorService {

    fun calculateTDEE(userInfo: UserInfo): Double {
        val bmr = when (userInfo.gender.lowercase()) {
            "male" -> 66 + (13.7 * userInfo.weight) + (5 * userInfo.height) - (6.8 * userInfo.age)
            "female" -> 655 + (9.6 * userInfo.weight) + (1.8 * userInfo.height) - (4.7 * userInfo.age)
            else -> throw IllegalArgumentException("Invalid gender")
        }

        return when (userInfo.activityLevel.lowercase()) {
            "low" -> bmr * 1.2
            "medium" -> bmr * 1.55
            "high" -> bmr * 1.9
            else -> throw IllegalArgumentException("Invalid activity level")
        }
    }

    fun calculateMacronutrients(tdee: Double, goal: String): CalculationResult {
        val (proteinRatio, fatRatio, carbsRatio) = when (goal.lowercase()) {
            "lose" -> Triple(0.3, 0.3, 0.4)
            "maintain" -> Triple(0.2, 0.3, 0.5)
            "gain" -> Triple(0.3, 0.3, 0.4)
            else -> throw IllegalArgumentException("Invalid goal")
        }

        val protein = (tdee * proteinRatio) / 4
        val fat = (tdee * fatRatio) / 9
        val carbs = (tdee * carbsRatio) / 4

        val recommendedCalories = when (goal.lowercase()) {
            "lose" -> tdee * 0.8
            "maintain" -> tdee
            "gain" -> tdee * 1.2
            else -> throw IllegalArgumentException("Invalid goal")
        }

        return CalculationResult(tdee, protein, fat, carbs, recommendedCalories)
    }
}