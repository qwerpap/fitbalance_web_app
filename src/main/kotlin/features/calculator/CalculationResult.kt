import kotlinx.serialization.Serializable

@Serializable
data class CalculationResult(
    val tdee: Double, // Total Daily Energy Expenditure
    val protein: Double, // in grams
    val fat: Double, // in grams
    val carbs: Double, // in grams
    val recommendedCalories: Double // based on goal
)