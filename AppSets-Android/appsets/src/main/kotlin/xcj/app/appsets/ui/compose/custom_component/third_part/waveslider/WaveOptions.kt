package xcj.app.appsets.ui.compose.custom_component.third_part.waveslider

/**
 * Provides customization of the sine wave section of
 * the slider
 *
 * @param amplitude - The height of the wave
 * @param frequency - How many waves appear in one section
 */
data class WaveOptions(
    val amplitude: Float,
    val frequency: Float,
    val trackWidth: Float
)
