package com.example.features.calculator.data.repositories

import com.example.features.calculator.CalculationResultDTO
import com.example.features.calculator.CalculationResultTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CalculationResultRepository {

    fun create(calculation: CalculationResultDTO): CalculationResultDTO {
        return transaction {
            CalculationResultTable.insert {
                it[id] = calculation.id
                it[userId] = calculation.userId
                it[tdee] = calculation.tdee
                it[protein] = calculation.protein
                it[fat] = calculation.fat
                it[carbs] = calculation.carbs
                it[recommendedCalories] = calculation.recommendedCalories
                it[createdAt] = calculation.createdAt
            }.resultedValues?.first()?.let { rowToCalculation(it) } ?: calculation
        }
    }

    fun read(id: String): CalculationResultDTO? {
        return transaction {
            CalculationResultTable
                .selectAll()
                .where { CalculationResultTable.id eq id }
                .map { rowToCalculation(it) }
                .firstOrNull()
        }
    }



    fun update(calculation: CalculationResultDTO): CalculationResultDTO? {
        return transaction {
            val updatedRows = CalculationResultTable.update({ CalculationResultTable.id eq calculation.id }) {
                it[userId] = calculation.userId
                it[tdee] = calculation.tdee
                it[protein] = calculation.protein
                it[fat] = calculation.fat
                it[carbs] = calculation.carbs
                it[recommendedCalories] = calculation.recommendedCalories
                it[createdAt] = calculation.createdAt
            }

            if (updatedRows > 0) calculation else null
        }
    }

    fun delete(id: String): Boolean {
        return transaction {
            val deletedCount = CalculationResultTable.deleteWhere {
                CalculationResultTable.id.eq(id)
            }
            deletedCount > 0
        }
    }

    fun findByUserId(userId: String): List<CalculationResultDTO> {
        return transaction {
            CalculationResultTable
                .selectAll()
                .where { CalculationResultTable.userId eq userId }
                .orderBy(CalculationResultTable.createdAt to SortOrder.DESC)
                .map { rowToCalculation(it) }
        }
    }

    private fun rowToCalculation(row: ResultRow): CalculationResultDTO {
        return CalculationResultDTO(
            id = row[CalculationResultTable.id],
            userId = row[CalculationResultTable.userId],
            tdee = row[CalculationResultTable.tdee],
            protein = row[CalculationResultTable.protein],
            fat = row[CalculationResultTable.fat],
            carbs = row[CalculationResultTable.carbs],
            recommendedCalories = row[CalculationResultTable.recommendedCalories],
            createdAt = row[CalculationResultTable.createdAt]
        )
    }
}