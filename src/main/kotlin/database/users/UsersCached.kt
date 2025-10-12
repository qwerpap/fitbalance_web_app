package com.example.database.users

import com.example.cache.CacheService
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/**
 * Обертка над Users с кэшированием
 */
object UsersCached {
    private val logger = LoggerFactory.getLogger(UsersCached::class.java)

    /**
     * Получить пользователя по login с кэшированием
     */
    fun fetchUser(login: String, cacheService: CacheService?): UserDTO? {
        val cacheKey = "${CacheService.USER_PREFIX}login:$login"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedUserDTO>(cacheKey)?.let { cached ->
            logger.debug("User fetched from cache: login=$login")
            return cached.toUserDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val user = Users.fetchUser(login)
        
        // Сохраняем в кэш
        user?.let {
            cacheService?.setObject(cacheKey, CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            logger.debug("User cached: login=$login")
        }
        
        return user
    }

    /**
     * Получить пользователя по Google ID с кэшированием
     */
    fun fetchUserByGoogleId(googleId: String, cacheService: CacheService?): UserDTO? {
        val cacheKey = "${CacheService.USER_GOOGLE_PREFIX}$googleId"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedUserDTO>(cacheKey)?.let { cached ->
            logger.debug("User fetched from cache: googleId=$googleId")
            return cached.toUserDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val user = Users.fetchUserByGoogleId(googleId)
        
        // Сохраняем в кэш
        user?.let {
            cacheService?.setObject(cacheKey, CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            // Также кэшируем по другим ключам
            cacheService?.setObject("${CacheService.USER_PREFIX}id:${it.id}", CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            cacheService?.setObject("${CacheService.USER_EMAIL_PREFIX}${it.email}", CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            logger.debug("User cached: googleId=$googleId")
        }
        
        return user
    }

    /**
     * Получить пользователя по email с кэшированием
     */
    fun fetchUserByEmail(email: String, cacheService: CacheService?): UserDTO? {
        val cacheKey = "${CacheService.USER_EMAIL_PREFIX}$email"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedUserDTO>(cacheKey)?.let { cached ->
            logger.debug("User fetched from cache: email=$email")
            return cached.toUserDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val user = Users.fetchUserByEmail(email)
        
        // Сохраняем в кэш
        user?.let {
            cacheService?.setObject(cacheKey, CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            logger.debug("User cached: email=$email")
        }
        
        return user
    }

    /**
     * Получить пользователя по ID с кэшированием
     */
    fun fetchUserById(userId: String, cacheService: CacheService?): UserDTO? {
        val cacheKey = "${CacheService.USER_PREFIX}id:$userId"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedUserDTO>(cacheKey)?.let { cached ->
            logger.debug("User fetched from cache: userId=$userId")
            return cached.toUserDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val user = Users.fetchUserById(userId)
        
        // Сохраняем в кэш
        user?.let {
            cacheService?.setObject(cacheKey, CachedUserDTO.fromUserDTO(it), CacheService.USER_TTL)
            logger.debug("User cached: userId=$userId")
        }
        
        return user
    }

    /**
     * Вставить пользователя и сохранить в кэш
     */
    fun insert(userDTO: UserDTO, cacheService: CacheService?) {
        // Сохраняем в БД
        Users.insert(userDTO)
        
        // Сохраняем в кэш по всем ключам
        val cachedUser = CachedUserDTO.fromUserDTO(userDTO)
        cacheService?.setObject("${CacheService.USER_PREFIX}id:${userDTO.id}", cachedUser, CacheService.USER_TTL)
        cacheService?.setObject("${CacheService.USER_GOOGLE_PREFIX}${userDTO.googleId}", cachedUser, CacheService.USER_TTL)
        cacheService?.setObject("${CacheService.USER_EMAIL_PREFIX}${userDTO.email}", cachedUser, CacheService.USER_TTL)
        userDTO.login?.let {
            cacheService?.setObject("${CacheService.USER_PREFIX}login:$it", cachedUser, CacheService.USER_TTL)
        }
        
        logger.debug("User inserted and cached: userId=${userDTO.id}")
    }

    /**
     * Обновить роль пользователя и инвалидировать кэш
     */
    fun updateUserRole(userId: String, newRole: String, cacheService: CacheService?): Boolean {
        val result = Users.updateUserRole(userId, newRole)
        
        if (result) {
            // Инвалидируем кэш пользователя
            invalidateUserCache(userId, cacheService)
            logger.debug("User role updated and cache invalidated: userId=$userId")
        }
        
        return result
    }

    /**
     * Обновить пользователя и инвалидировать кэш
     */
    fun updateUser(login: String, newUserDTO: UserDTO, cacheService: CacheService?) {
        Users.updateUser(login, newUserDTO)
        
        // Инвалидируем кэш пользователя
        invalidateUserCache(newUserDTO.id, cacheService)
        logger.debug("User updated and cache invalidated: login=$login")
    }

    /**
     * Удалить пользователя и инвалидировать кэш
     */
    fun deleteUser(login: String, cacheService: CacheService?) {
        // Сначала получаем пользователя для инвалидации всех кэшей
        val user = Users.fetchUser(login)
        
        Users.deleteUser(login)
        
        // Инвалидируем все кэши пользователя
        user?.let {
            invalidateUserCache(it.id, cacheService)
            logger.debug("User deleted and cache invalidated: login=$login")
        }
    }

    /**
     * Инвалидировать все кэши пользователя
     */
    private fun invalidateUserCache(userId: String, cacheService: CacheService?) {
        // Получаем пользователя из БД для удаления всех связанных кэшей
        val user = Users.fetchUserById(userId)
        
        user?.let {
            cacheService?.delete("${CacheService.USER_PREFIX}id:${it.id}")
            cacheService?.delete("${CacheService.USER_GOOGLE_PREFIX}${it.googleId}")
            cacheService?.delete("${CacheService.USER_EMAIL_PREFIX}${it.email}")
            it.login?.let { login ->
                cacheService?.delete("${CacheService.USER_PREFIX}login:$login")
            }
        }
    }

    /**
     * Получить всех пользователей (без кэширования, т.к. это редкая операция)
     */
    fun fetchAll(): List<UserDTO> {
        return Users.fetchAll()
    }
}

/**
 * Serializable версия UserDTO для кэширования
 */
@Serializable
data class CachedUserDTO(
    val id: String,
    val googleId: String,
    val email: String,
    val login: String?,
    val password: String?,
    val role: String
) {
    fun toUserDTO(): UserDTO {
        return UserDTO(
            id = id,
            googleId = googleId,
            email = email,
            login = login,
            password = password,
            role = role
        )
    }

    companion object {
        fun fromUserDTO(userDTO: UserDTO): CachedUserDTO {
            return CachedUserDTO(
                id = userDTO.id,
                googleId = userDTO.googleId,
                email = userDTO.email,
                login = userDTO.login,
                password = userDTO.password,
                role = userDTO.role
            )
        }
    }
}

