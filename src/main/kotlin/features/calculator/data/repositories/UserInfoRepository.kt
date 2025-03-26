package com.example.features.calculator.data.repositories

import com.example.features.calculator.UserInfoDTO
import com.example.features.calculator.UserInfoTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserInfoRepository {

    //CREATE
    fun insert(userInfoDTO: UserInfoDTO) {
        transaction {
            UserInfoTable.insert {
                it[id] = userInfoDTO.id
                it[age] = userInfoDTO.age
                it[gender] = userInfoDTO.gender
                it[height] = userInfoDTO.height
                it[weight] = userInfoDTO.weight
                it[goal] = userInfoDTO.goal
                it[activityLevel] = userInfoDTO.activityLevel
            }
        }
    }

    //READ
    fun fetchUserInfo(id: String): UserInfoDTO? {
        return transaction {
            UserInfoTable.selectAll().where { UserInfoTable.id eq id }.singleOrNull()?.let {
                UserInfoDTO(
                    id = it[UserInfoTable.id],
                    age = it[UserInfoTable.age],
                    gender = it[UserInfoTable.gender],
                    height = it[UserInfoTable.height],
                    weight = it[UserInfoTable.weight],
                    goal = it[UserInfoTable.goal],
                    activityLevel = it[UserInfoTable.activityLevel]
                )
            }
        }
    }

    //UPDATE
    fun update(userInfoDTO: UserInfoDTO) {
        transaction {
            UserInfoTable.update({ UserInfoTable.id eq userInfoDTO.id }) {
                it[age] = userInfoDTO.age
                it[gender] = userInfoDTO.gender
                it[height] = userInfoDTO.height
                it[weight] = userInfoDTO.weight
                it[goal] = userInfoDTO.goal
                it[activityLevel] = userInfoDTO.activityLevel
            }
        }
    }

    //DELETE
    fun delete(id: String) {
        transaction {
            UserInfoTable.deleteWhere { UserInfoTable.id eq id }
        }
    }

}
