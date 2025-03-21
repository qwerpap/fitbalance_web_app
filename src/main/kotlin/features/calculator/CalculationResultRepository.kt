package com.example.features.calculator

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CalculationResultRepository {

    fun insert(calculationResultDTO: CalculationResultDTO) {
        transaction {
            CalculationResultTable.insert {
                it[id] = calculationResultDTO.id
                it[userId] = calculationResultDTO.userId
                it[tdee] = calculationResultDTO.tdee
                it[protein] = calculationResultDTO.protein
                it[fat] = calculationResultDTO.fat
                it[carbs] = calculationResultDTO.carbs
                it[recommendedCalories] = calculationResultDTO.recommendedCalories
            }
        }
    }

    fun fetchCalculationResult(userId: String): CalculationResultDTO? {
        return transaction {
            CalculationResultTable.selectAll().where { CalculationResultTable.userId eq userId }.singleOrNull()?.let {
                CalculationResultDTO(
                    id = it[CalculationResultTable.id],
                    userId = it[CalculationResultTable.userId],
                    tdee = it[CalculationResultTable.tdee],
                    protein = it[CalculationResultTable.protein],
                    fat = it[CalculationResultTable.fat],
                    carbs = it[CalculationResultTable.carbs],
                    recommendedCalories = it[CalculationResultTable.recommendedCalories]
                )
            }
        }
    }
}