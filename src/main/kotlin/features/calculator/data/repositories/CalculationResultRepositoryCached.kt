package com.example.features.calculator.data.repositories

import com.example.cache.CacheService
import com.example.features.calculator.CalculationResultDTO
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/**
 * Обертка над CalculationResultRepository с кэшированием
 */
object CalculationResultRepositoryCached {
    private val logger = LoggerFactory.getLogger(CalculationResultRepositoryCached::class.java)

    /**
     * Создать результат расчета и сохранить в кэш
     */
    fun create(calculation: CalculationResultDTO, cacheService: CacheService?): CalculationResultDTO {
        // Сохраняем в БД
        val result = CalculationResultRepository.create(calculation)
        
        // Сохраняем в кэш
        val cacheKey = "${CacheService.CALCULATION_PREFIX}${result.id}"
        cacheService?.setObject(cacheKey, CachedCalculationDTO.fromCalculationDTO(result), CacheService.CALCULATION_TTL)
        
        // Также кэшируем по userId для быстрого доступа к списку расчетов
        val userCalculationsKey = "${CacheService.CALCULATION_PREFIX}user:${result.userId}:latest"
        cacheService?.setObject(userCalculationsKey, CachedCalculationDTO.fromCalculationDTO(result), CacheService.CALCULATION_TTL)
        
        logger.debug("Calculation created and cached: id=${result.id}, userId=${result.userId}")
        
        return result
    }

    /**
     * Получить результат расчета по ID с кэшированием
     */
    fun read(id: String, cacheService: CacheService?): CalculationResultDTO? {
        val cacheKey = "${CacheService.CALCULATION_PREFIX}$id"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedCalculationDTO>(cacheKey)?.let { cached ->
            logger.debug("Calculation fetched from cache: id=$id")
            return cached.toCalculationDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val calculation = CalculationResultRepository.read(id)
        
        // Сохраняем в кэш
        calculation?.let {
            cacheService?.setObject(cacheKey, CachedCalculationDTO.fromCalculationDTO(it), CacheService.CALCULATION_TTL)
            logger.debug("Calculation cached: id=$id")
        }
        
        return calculation
    }

    /**
     * Обновить результат расчета и инвалидировать кэш
     */
    fun update(calculation: CalculationResultDTO, cacheService: CacheService?): CalculationResultDTO? {
        val result = CalculationResultRepository.update(calculation)
        
        // Инвалидируем кэш
        result?.let {
            invalidateCalculationCache(it.id, it.userId, cacheService)
            logger.debug("Calculation updated and cache invalidated: id=${it.id}")
        }
        
        return result
    }

    /**
     * Удалить результат расчета и инвалидировать кэш
     */
    fun delete(id: String, cacheService: CacheService?): Boolean {
        // Сначала получаем расчет для инвалидации кэша
        val calculation = CalculationResultRepository.read(id)
        
        val result = CalculationResultRepository.delete(id)
        
        // Инвалидируем кэш
        if (result && calculation != null) {
            invalidateCalculationCache(calculation.id, calculation.userId, cacheService)
            logger.debug("Calculation deleted and cache invalidated: id=$id")
        }
        
        return result
    }

    /**
     * Получить список расчетов пользователя с кэшированием последнего
     */
    fun findByUserId(userId: String, cacheService: CacheService?): List<CalculationResultDTO> {
        // Для списка не кэшируем, т.к. он может меняться
        // Но можно добавить кэширование если нужно
        val calculations = CalculationResultRepository.findByUserId(userId)
        
        // Кэшируем последний результат для быстрого доступа
        calculations.firstOrNull()?.let { latest ->
            val latestKey = "${CacheService.CALCULATION_PREFIX}user:$userId:latest"
            cacheService?.setObject(latestKey, CachedCalculationDTO.fromCalculationDTO(latest), CacheService.CALCULATION_TTL)
        }
        
        logger.debug("Calculations fetched for user: userId=$userId, count=${calculations.size}")
        
        return calculations
    }

    /**
     * Получить последний расчет пользователя с кэшированием
     */
    fun getLatestByUserId(userId: String, cacheService: CacheService?): CalculationResultDTO? {
        val latestKey = "${CacheService.CALCULATION_PREFIX}user:$userId:latest"
        
        // Пытаемся получить из кэша
        cacheService?.getObject<CachedCalculationDTO>(latestKey)?.let { cached ->
            logger.debug("Latest calculation fetched from cache: userId=$userId")
            return cached.toCalculationDTO()
        }
        
        // Если не нашли в кэше, идем в БД
        val calculations = CalculationResultRepository.findByUserId(userId)
        val latest = calculations.firstOrNull()
        
        // Сохраняем в кэш
        latest?.let {
            cacheService?.setObject(latestKey, CachedCalculationDTO.fromCalculationDTO(it), CacheService.CALCULATION_TTL)
            logger.debug("Latest calculation cached: userId=$userId")
        }
        
        return latest
    }

    /**
     * Инвалидировать кэш результата расчета
     */
    private fun invalidateCalculationCache(id: String, userId: String?, cacheService: CacheService?) {
        cacheService?.delete("${CacheService.CALCULATION_PREFIX}$id")
        userId?.let {
            cacheService?.delete("${CacheService.CALCULATION_PREFIX}user:$it:latest")
        }
        logger.debug("Cache invalidated for calculation: id=$id, userId=$userId")
    }
}

/**
 * Serializable версия CalculationResultDTO для кэширования
 */
@Serializable
data class CachedCalculationDTO(
    val id: String,
    val userId: String?,
    val tdee: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val recommendedCalories: Double,
    val createdAt: Long
) {
    fun toCalculationDTO(): CalculationResultDTO {
        return CalculationResultDTO(
            id = id,
            userId = userId,
            tdee = tdee,
            protein = protein,
            fat = fat,
            carbs = carbs,
            recommendedCalories = recommendedCalories,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromCalculationDTO(dto: CalculationResultDTO): CachedCalculationDTO {
            return CachedCalculationDTO(
                id = dto.id,
                userId = dto.userId,
                tdee = dto.tdee,
                protein = dto.protein,
                fat = dto.fat,
                carbs = dto.carbs,
                recommendedCalories = dto.recommendedCalories,
                createdAt = dto.createdAt
            )
        }
    }
}

