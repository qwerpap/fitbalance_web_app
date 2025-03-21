package com.example.features.calculator

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserInfoRepository {

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
}
