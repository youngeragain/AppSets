/*   Copyright 2023 Sebastian Hriscu
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package xcj.app.appsets.ui.compose.custom_component.third_part.waveslider

import androidx.compose.runtime.Stable

/**
 * @param reverseDirection Whether to animate the wave in the reverse direction.
 * (By default the wave moves to the right)
 * @param flatlineOnDrag Whether to have the slider become a straight line when dragged
 * and be a wave when released
 * @param animateWave Whether to animate the wave infinitely
 * @param reverseFlatline Have the slider be a flat line by default and show the wave
 * when dragged
 * @param animationSpeedMs How fast the wave moves, this is the time in milliseconds
 * that it will take one period of the wave to travel the distance of the slider
 */
@Stable
data class WaveAnimationOptions(
    val reverseDirection: Boolean = false,
    val flatlineOnDrag: Boolean = true,
    val animateWave: Boolean = true,
    val reverseFlatline: Boolean = false,
    val animationSpeedMs: Int = 10000
)
