package com.example.features.calculator

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CalculationResultTable : Table("calculation_result") {
    val id = varchar("id", 50).uniqueIndex()
    val userId = varchar("user_id", 50).references(UserInfoTable.id, onDelete = ReferenceOption.CASCADE) // Вот каскадное удаление
    val tdee = double("tdee")
    val protein = double("protein")
    val fat = double("fat")
    val carbs = double("carbs")
    val recommendedCalories = double("recommended_calories")

    override val primaryKey = PrimaryKey(id, name = "PK_Calculation_ID")
}