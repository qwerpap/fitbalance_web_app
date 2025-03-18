package com.example.database.tokens

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens: Table() {
    private val id = Tokens.varchar("id", 50)
    private val login = Tokens.varchar("login", 25)
    private val token = Tokens.varchar("token", 255)


    fun insert(TokenDTO: TokenDTO) {
        transaction {
            Tokens.insert {
                it[id] = TokenDTO.rowId
                it[login] = TokenDTO.login
                it[token] = TokenDTO.token
            }
        }
    }
}