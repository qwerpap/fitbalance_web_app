package com.example.features.calculator

import org.jetbrains.exposed.sql.Table

object UserInfoTable : Table("user_info") {
    val id = varchar("id", 50).uniqueIndex() // Это будет primary key
    val age = integer("age")
    val gender = varchar("gender", 10)
    val height = double("height")
    val weight = double("weight")
    val goal = varchar("goal", 20)
    val activityLevel = varchar("activity_level", 20)

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}