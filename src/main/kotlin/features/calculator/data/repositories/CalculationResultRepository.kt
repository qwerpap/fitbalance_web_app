package com.example.features.calculator.data.repositories

import com.example.features.calculator.CalculationResultDTO
import com.example.features.calculator.CalculationResultTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object CalculationResultRepository {

    // CREATE
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

//    // READ
//    fun fetchCalculationResult(id: String): CalculationResultDTO? {
//        return transaction {
//            CalculationResultTable.select { CalculationResultTable.id eq id }.singleOrNull()?.let {
//                CalculationResultDTO(
//                    id = it[CalculationResultTable.id],
//                    userId = it[CalculationResultTable.userId],
//                    tdee = it[CalculationResultTable.tdee],
//                    protein = it[CalculationResultTable.protein],
//                    fat = it[CalculationResultTable.fat],
//                    carbs = it[CalculationResultTable.carbs],
//                    recommendedCalories = it[CalculationResultTable.recommendedCalories]
//                )
//            }
//        }
//    }

    // UPDATE
    fun update(calculationResultDTO: CalculationResultDTO) {
        transaction {
            CalculationResultTable.update({ CalculationResultTable.id eq calculationResultDTO.id }) {
                it[userId] = calculationResultDTO.userId
                it[tdee] = calculationResultDTO.tdee
                it[protein] = calculationResultDTO.protein
                it[fat] = calculationResultDTO.fat
                it[carbs] = calculationResultDTO.carbs
                it[recommendedCalories] = calculationResultDTO.recommendedCalories
            }
        }
    }

    // DELETE
    fun delete(id: String) {
        transaction {
            CalculationResultTable.deleteWhere { CalculationResultTable.id eq id }
        }
    }
}