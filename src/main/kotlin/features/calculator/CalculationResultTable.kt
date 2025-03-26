package com.example.features.calculator

import org.jetbrains.exposed.sql.Table

object CalculationResultTable : Table("calculation_result") {
    val id = varchar("id", 50).uniqueIndex()  // Уникальный идентификатор результата
    val userId = varchar("user_id", 50).references(UserInfoTable.id) // Внешний ключ
    val tdee = double("tdee")  // Total Daily Energy Expenditure
    val protein = double("protein")  // Белки в граммах
    val fat = double("fat")  // Жиры в граммах
    val carbs = double("carbs")  // Углеводы в граммах
    val recommendedCalories = double("recommended_calories")  // Рекомендуемые калории
}