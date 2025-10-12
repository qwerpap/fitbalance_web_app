package com.example.database.users

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object Users : Table() {
    private val id = Users.varchar("id", 36)
    private val googleId = Users.varchar("google_id", 100)
    private val email = Users.varchar("email", 100)
    private val login = Users.varchar("login", 50).nullable()
    private val password = Users.varchar("password", 100).nullable()
    private val role = Users.varchar("role", 25)

    // CRUD операции

    fun insert(userDTO: UserDTO) {
        transaction {
            Users.insert {
                it[id] = userDTO.id
                it[googleId] = userDTO.googleId
                it[email] = userDTO.email
                it[login] = userDTO.login
                it[password] = userDTO.password
                it[role] = userDTO.role
            }
        }
    }

    fun fetchUser(login: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.login eq login }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        id = it[Users.id],
                        googleId = it[Users.googleId],
                        email = it[Users.email],
                        login = it[Users.login],
                        password = it[Users.password],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching user by login: ${e.message}")
            null
        }
    }

    fun fetchUserByGoogleId(googleId: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.googleId eq googleId }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        id = it[Users.id],
                        googleId = it[Users.googleId],
                        email = it[Users.email],
                        login = it[Users.login],
                        password = it[Users.password],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching user by Google ID: ${e.message}")
            null
        }
    }

    fun fetchUserByEmail(email: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.email eq email }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        id = it[Users.id],
                        googleId = it[Users.googleId],
                        email = it[Users.email],
                        login = it[Users.login],
                        password = it[Users.password],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching user by email: ${e.message}")
            null
        }
    }

    fun fetchUserById(userId: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.id eq userId }.singleOrNull()
                userModel?.let {
                    UserDTO(
                        id = it[Users.id],
                        googleId = it[Users.googleId],
                        email = it[Users.email],
                        login = it[Users.login],
                        password = it[Users.password],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching user by ID: ${e.message}")
            null
        }
    }

    fun fetchAll(): List<UserDTO> {
        return try {
            transaction {
                Users.selectAll().map {
                    UserDTO(
                        id = it[Users.id],
                        googleId = it[Users.googleId],
                        email = it[Users.email],
                        login = it[Users.login],
                        password = it[Users.password],
                        role = it[Users.role]
                    )
                }
            }
        } catch (e: Exception) {
            println("Error fetching all users: ${e.message}")
            emptyList()
        }
    }

    fun updateUserRole(userId: String, newRole: String): Boolean {
        return try {
            transaction {
                Users.update({ Users.id eq userId }) {
                    it[role] = newRole
                } > 0
            }
        } catch (e: Exception) {
            println("Error updating user role: ${e.message}")
            false
        }
    }

    fun updateUser(login: String, newUserDTO: UserDTO) {
        transaction {
            Users.update({ Users.login eq login }) {
                it[password] = newUserDTO.password
                it[email] = newUserDTO.email
            }
        }
    }

    fun deleteUser(login: String) {
        transaction {
            Users.deleteWhere { Users.login eq login }
        }
    }
}
