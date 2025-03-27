package com.example.database.users

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

object Users: Table() {
    private val id = Users.varchar("id", 36)
    private val login = Users.varchar("login", 25)
    private val password = Users.varchar("password", 25)
    private val email = Users.varchar("email", 25).nullable()
    private  val role = Users.varchar("role", 25)

    //CRUD

    fun insert(userDTO: UserDTO) {
        transaction {
            Users.insert {
                it[id] = userDTO.id
                it[login] = userDTO.login
                it[password] = userDTO.password
                it[email] = userDTO.email ?: ""
                it[role] = userDTO.role ?: "user"
            }
        }
    }


    // val userModel = Users.select(Users.login eq login).singleOrNull()

    fun fetchUser(login: String): UserDTO? {
        return try {
            transaction {
                // Используем where для запроса
                val userModel = Users.selectAll().where { Users.login eq login }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        login = it[Users.login],
                        password = it[Users.password],
                        email = it[Users.email],
                        id = it[Users.id],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching user: ${e.message}")
            null
        }
    }

    fun updateUser(
        login: String,
        newUserDTO: UserDTO
    ) {         //Updata -> обновляет данные пользователя по логину
        transaction {
            Users.update({ Users.login eq login }) {
                it[password] = newUserDTO.password
                it[email] = newUserDTO.email ?: ""
            }
        }
    }


    fun deleteUser(login: String) {
        transaction {
            Users.deleteWhere { Users.login eq login }
        }
    }
}


