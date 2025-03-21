package com.example.features.calculator

import org.jetbrains.exposed.sql.Table

object UserInfoTable : Table("user_info") {
    val id = varchar("id", 50).uniqueIndex()  // Уникальный идентификатор пользователя
    val age = integer("age")  // Возраст
    val gender = varchar("gender", 10)  // Пол ("male" или "female")
    val height = double("height")  // Рост в см
    val weight = double("weight")  // Вес в кг
    val goal = varchar("goal", 10)  // Цель ("lose", "maintain", "gain")
    val activityLevel = varchar("activity_level", 10)  // Уровень активности ("low", "medium", "high")
}