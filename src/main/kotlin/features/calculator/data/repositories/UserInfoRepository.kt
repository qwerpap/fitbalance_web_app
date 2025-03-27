package com.example.features.calculator.data.repositories

import com.example.features.calculator.UserInfoDTO
import com.example.features.calculator.UserInfoTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

import org.jetbrains.exposed.sql.*


object UserInfoRepository {

    fun create(userInfo: UserInfoDTO): UserInfoDTO {
        return transaction {
            UserInfoTable.insert {
                it[id] = userInfo.id
                it[age] = userInfo.age
                it[gender] = userInfo.gender
                it[height] = userInfo.height
                it[weight] = userInfo.weight
                it[goal] = userInfo.goal
                it[activityLevel] = userInfo.activityLevel
            }.resultedValues?.first()?.let { rowToUserInfo(it) } ?: userInfo
        }
    }
    fun read(id: String): UserInfoDTO? {
        return transaction {
            UserInfoTable
                .selectAll() // Выбираем все столбцы
                .where { UserInfoTable.id eq id } // Фильтруем по id
                .mapNotNull { rowToUserInfo(it) }
                .firstOrNull()
        }
    }


    fun update(userInfo: UserInfoDTO): UserInfoDTO? {
        return transaction {
            val updatedRows = UserInfoTable.update({ UserInfoTable.id eq userInfo.id }) {
                it[age] = userInfo.age
                it[gender] = userInfo.gender
                it[height] = userInfo.height
                it[weight] = userInfo.weight
                it[goal] = userInfo.goal
                it[activityLevel] = userInfo.activityLevel
            }

            if (updatedRows > 0) userInfo else null
        }
    }

    fun delete(id: String): Boolean {
        return transaction {
            UserInfoTable.deleteWhere { UserInfoTable.id eq id } > 0
        }
    }

    private fun rowToUserInfo(row: ResultRow): UserInfoDTO {
        return UserInfoDTO(
            id = row[UserInfoTable.id],
            age = row[UserInfoTable.age],
            gender = row[UserInfoTable.gender],
            height = row[UserInfoTable.height],
            weight = row[UserInfoTable.weight],
            goal = row[UserInfoTable.goal],
            activityLevel = row[UserInfoTable.activityLevel]
        )
    }
}
