package com.example.features.calculator

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CalculationResultTable : Table("calculation_result") {
    val id = varchar("id", 50).uniqueIndex()
    val userId = varchar("user_id", 50).nullable() // Разрешаем null значения
    val tdee = double("tdee")
    val protein = double("protein")
    val fat = double("fat")
    val carbs = double("carbs")
    val recommendedCalories = double("recommended_calories")
    val createdAt = long("created_at") // Временная метка
}