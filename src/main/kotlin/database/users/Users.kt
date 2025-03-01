package com.example.database.users

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table("users") {
    private val login = Users.varchar("login", 25)
    private val password = Users.varchar("password", 25)
    private val email = Users.varchar("email", 25)

    //CRUT
    fun insert(userDTO: UserDTO) {
        transaction {
            Users.insert {
                it[login] = userDTO.login
                it[password] = userDTO.password
                it[email] = userDTO.email ?: ""
            }
        }
    }

    fun fetchUser(login: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.select(Users.login eq login).singleOrNull()
                userModel?.let {
                    UserDTO(
                        login = it[Users.login],
                        password = it[password],
                        email = it[email]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}