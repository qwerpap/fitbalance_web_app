package com.example.database.tokens

import com.example.features.calculator.CalculationResultTable.userId
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens: Table() {
    private val id = Tokens.varchar("id", 50)
    private val login = Tokens.varchar("login", 25)
    private val token = Tokens.varchar("token", 255)


    fun insert(tokenDTO: TokenDTO) {
        transaction {
            Tokens.insert {
                it[id] = tokenDTO.rowId
                it[userId] = tokenDTO.userId
                it[login] = tokenDTO.login
                it[token] = tokenDTO.token
            }
        }
    }
}