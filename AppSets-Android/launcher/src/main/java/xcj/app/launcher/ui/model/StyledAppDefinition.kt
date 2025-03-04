package xcj.app.launcher.ui.model

import xcj.app.starter.android.AppDefinition

data class AppStyle(
    val sizeStyleH: Int = SIZE_1_OF_4,
    val sizeStyleV: Int = SIZE_1_OF_4
) {
    companion object {
        const val SIZE_1_OF_4 = 1
        const val SIZE_2_OF_4 = 2
        const val SIZE_3_OF_4 = 3
        const val SIZE_4_OF_4 = 4
    }

    fun previousSizeStyleH(): AppStyle? {
        if (sizeStyleH == SIZE_1_OF_4) {
            return null
        }
        return copy(sizeStyleH = sizeStyleH - 1)
    }

    fun nextSizeStyleH(): AppStyle? {
        if (sizeStyleH == SIZE_4_OF_4) {
            return null
        }
        return copy(sizeStyleH = sizeStyleH + 1)
    }

    fun previousSizeStyleV(): AppStyle? {
        if (sizeStyleH == SIZE_1_OF_4) {
            return null
        }
        return copy(sizeStyleV = sizeStyleH - 1)
    }

    fun nextSizeStyleV(): AppStyle? {
        if (sizeStyleV == SIZE_4_OF_4) {
            return null
        }
        return copy(sizeStyleV = sizeStyleV + 1)
    }
}

data class StyledAppDefinition(
    val appDefinition: AppDefinition,
    val style: AppStyle
)