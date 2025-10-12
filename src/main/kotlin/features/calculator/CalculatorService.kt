package com.example.features.calculator
import com.example.features.calculator.CalculationResult

class CalculatorService {

    /**
     * Полный расчет: BMI, BMR, TDEE и макронутриенты
     */
    fun calculate(
        weight: Double,
        height: Double,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ): CalculationResultData {
        // BMI (Body Mass Index)
        val heightInMeters = height / 100.0
        val bmi = weight / (heightInMeters * heightInMeters)
        
        // BMR (Basal Metabolic Rate)
        val bmr = when (gender.lowercase()) {
            "male" -> 66 + (13.7 * weight) + (5 * height) - (6.8 * age)
            "female" -> 655 + (9.6 * weight) + (1.8 * height) - (4.7 * age)
            else -> throw IllegalArgumentException("Invalid gender: $gender")
        }
        
        // TDEE (Total Daily Energy Expenditure)
        val tdee = when (activityLevel.lowercase()) {
            "sedentary", "low" -> bmr * 1.2
            "moderate", "medium" -> bmr * 1.55
            "active", "high" -> bmr * 1.9
            else -> throw IllegalArgumentException("Invalid activity level: $activityLevel")
        }
        
        // Рекомендуемые калории в зависимости от цели
        val recommendedCalories = when (goal.lowercase()) {
            "lose", "weight_loss" -> tdee * 0.8
            "maintain" -> tdee
            "gain", "muscle_gain" -> tdee * 1.2
            else -> throw IllegalArgumentException("Invalid goal: $goal")
        }
        
        // Макронутриенты
        val (proteinRatio, fatRatio, carbsRatio) = when (goal.lowercase()) {
            "lose", "weight_loss" -> Triple(0.3, 0.3, 0.4)
            "maintain" -> Triple(0.2, 0.3, 0.5)
            "gain", "muscle_gain" -> Triple(0.3, 0.3, 0.4)
            else -> Triple(0.25, 0.3, 0.45)
        }
        
        val protein = (recommendedCalories * proteinRatio) / 4  // 4 калории на грамм
        val fat = (recommendedCalories * fatRatio) / 9         // 9 калорий на грамм
        val carbs = (recommendedCalories * carbsRatio) / 4     // 4 калории на грамм
        
        return CalculationResultData(
            bmi = bmi,
            bmr = bmr,
            tdee = tdee,
            recommendedCalories = recommendedCalories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
    }

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

/**
 * Результат расчета для очередей
 */
data class CalculationResultData(
    val bmi: Double,
    val bmr: Double,
    val tdee: Double,
    val recommendedCalories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)