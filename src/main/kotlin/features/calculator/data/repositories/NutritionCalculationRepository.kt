package com.example.features.calculator.data.repositories

import com.example.features.calculator.data.models.NutritionCalculation
import java.util.concurrent.ConcurrentHashMap

object NutritionCalculationRepository {
    private val calculations = ConcurrentHashMap<String, NutritionCalculation>()

    // Создание нового расчета
    fun create(calculation: NutritionCalculation): NutritionCalculation {
        calculations[calculation.id] = calculation
        return calculation
    }

    // Получение расчета по ID
    fun read(id: String): NutritionCalculation? {
        return calculations[id]
    }

    // Обновление расчета
    fun update(calculation: NutritionCalculation): NutritionCalculation? {
        return if (calculations.containsKey(calculation.id)) {
            calculations[calculation.id] = calculation
            calculation
        } else {
            null
        }
    }

    // Удаление расчета
    fun delete(id: String): Boolean {
        return calculations.remove(id) != null
    }

    // Получение всех расчетов для конкретного пользователя
    fun findByUserId(userId: String): List<NutritionCalculation> {
        return calculations.values.filter { it.userId == userId }
    }
}