package com.example.cache

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory

/**
 * Сервис для работы с Redis кэшем
 */
class CacheService(redisUrl: String = "redis://localhost:6379") {
    private val logger = LoggerFactory.getLogger(CacheService::class.java)
    private val redisClient: RedisClient = RedisClient.create(redisUrl)
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val syncCommands: RedisCommands<String, String> = connection.sync()

    /**
     * Сохранить значение в кэш
     */
    public fun set(key: String, value: String, ttlSeconds: Long? = null) {
        try {
            if (ttlSeconds != null) {
                syncCommands.setex(key, ttlSeconds, value)
            } else {
                syncCommands.set(key, value)
            }
            logger.debug("Cache SET: key=$key, ttl=$ttlSeconds")
        } catch (e: Exception) {
            logger.error("Error setting cache key $key: ${e.message}", e)
        }
    }

    /**
     * Получить значение из кэша
     */
    public fun get(key: String): String? {
        return try {
            val value = syncCommands.get(key)
            logger.debug("Cache GET: key=$key, found=${value != null}")
            value
        } catch (e: Exception) {
            logger.error("Error getting cache key $key: ${e.message}", e)
            null
        }
    }

    /**
     * Удалить значение из кэша
     */
    fun delete(key: String): Boolean {
        return try {
            val result = syncCommands.del(key) > 0
            logger.debug("Cache DELETE: key=$key, success=$result")
            result
        } catch (e: Exception) {
            logger.error("Error deleting cache key $key: ${e.message}", e)
            false
        }
    }

    /**
     * Проверить существование ключа
     */
    fun exists(key: String): Boolean {
        return try {
            val result = syncCommands.exists(key) > 0
            logger.debug("Cache EXISTS: key=$key, exists=$result")
            result
        } catch (e: Exception) {
            logger.error("Error checking cache key $key: ${e.message}", e)
            false
        }
    }

    /**
     * Установить время жизни для ключа
     */
    fun expire(key: String, ttlSeconds: Long): Boolean {
        return try {
            val result = syncCommands.expire(key, ttlSeconds)
            logger.debug("Cache EXPIRE: key=$key, ttl=$ttlSeconds, success=$result")
            result
        } catch (e: Exception) {
            logger.error("Error setting expiration for cache key $key: ${e.message}", e)
            false
        }
    }

    /**
     * Получить оставшееся время жизни ключа
     */
    fun ttl(key: String): Long {
        return try {
            syncCommands.ttl(key)
        } catch (e: Exception) {
            logger.error("Error getting TTL for cache key $key: ${e.message}", e)
            -1
        }
    }

    /**
     * Сохранить объект в кэш (сериализация в JSON)
     */
    inline fun <reified T> setObject(key: String, value: T, ttlSeconds: Long? = null) {
        try {
            val json = Json.encodeToString(value)
            set(key, json, ttlSeconds)
        } catch (e: Exception) {
            println("Error serializing and caching object for key $key: ${e.message}")
        }
    }

    /**
     * Получить объект из кэша (десериализация из JSON)
     */
    inline fun <reified T> getObject(key: String): T? {
        return try {
            val json = get(key) ?: return null
            Json.decodeFromString<T>(json)
        } catch (e: Exception) {
            println("Error deserializing cached object for key $key: ${e.message}")
            null
        }
    }

    /**
     * Добавить значение в Set
     */
    fun sAdd(key: String, vararg members: String): Long {
        return try {
            val result = syncCommands.sadd(key, *members)
            logger.debug("Cache SADD: key=$key, added=$result")
            result
        } catch (e: Exception) {
            logger.error("Error adding to set $key: ${e.message}", e)
            0
        }
    }

    /**
     * Проверить, существует ли элемент в Set
     */
    fun sIsMember(key: String, member: String): Boolean {
        return try {
            val result = syncCommands.sismember(key, member)
            logger.debug("Cache SISMEMBER: key=$key, member=$member, exists=$result")
            result
        } catch (e: Exception) {
            logger.error("Error checking set membership for key $key: ${e.message}", e)
            false
        }
    }

    /**
     * Удалить элемент из Set
     */
    fun sRem(key: String, vararg members: String): Long {
        return try {
            val result = syncCommands.srem(key, *members)
            logger.debug("Cache SREM: key=$key, removed=$result")
            result
        } catch (e: Exception) {
            logger.error("Error removing from set $key: ${e.message}", e)
            0
        }
    }

    /**
     * Проверка подключения к Redis
     */
    fun ping(): Boolean {
        return try {
            val response = syncCommands.ping()
            logger.info("Redis PING: $response")
            response == "PONG"
        } catch (e: Exception) {
            logger.error("Redis connection failed: ${e.message}", e)
            false
        }
    }

    /**
     * Закрыть соединение с Redis
     */
    fun close() {
        try {
            connection.close()
            redisClient.shutdown()
            logger.info("Redis connection closed")
        } catch (e: Exception) {
            logger.error("Error closing Redis connection: ${e.message}", e)
        }
    }

    companion object {
        // Префиксы для разных типов данных
        const val USER_PREFIX = "user:"
        const val USER_GOOGLE_PREFIX = "user:google:"
        const val USER_EMAIL_PREFIX = "user:email:"
        const val OAUTH_CODE_PREFIX = "oauth:code:"
        const val SESSION_PREFIX = "session:"
        const val CALCULATION_PREFIX = "calculation:"
        
        // TTL константы (в секундах)
        const val USER_TTL = 3600L // 1 час
        const val OAUTH_CODE_TTL = 600L // 10 минут
        const val SESSION_TTL = 86400L // 24 часа
        const val CALCULATION_TTL = 7200L // 2 часа
    }
}

