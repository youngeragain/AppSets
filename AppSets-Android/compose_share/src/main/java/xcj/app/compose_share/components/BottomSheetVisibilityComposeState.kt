@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.compose_share.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties

data class DesignModalBottomSheetProperties(
    var shouldDismissOnBackPress: Boolean = true,
    var shouldDismissOnClickOutside: Boolean = true,
    var autoReset: Boolean = true
)

class BottomSheetVisibilityComposeState(
    val bottomSheetProperties: DesignModalBottomSheetProperties = DesignModalBottomSheetProperties()
) : ProgressiveVisibilityComposeState() {

    fun getFoundationDesignModalBottomSheetProperties(): ModalBottomSheetProperties {
        return ModalBottomSheetProperties(
            shouldDismissOnBackPress = bottomSheetProperties.shouldDismissOnBackPress,
            shouldDismissOnClickOutside = bottomSheetProperties.shouldDismissOnClickOutside
        )

    }

    fun makeCannotDismiss() {
        bottomSheetProperties.shouldDismissOnBackPress = false
        bottomSheetProperties.shouldDismissOnClickOutside = false
    }

    fun makeCanDismiss() {
        bottomSheetProperties.shouldDismissOnBackPress = true
        bottomSheetProperties.shouldDismissOnClickOutside = true
    }

    fun resetIfNeeded() {
        if (bottomSheetProperties.autoReset) {
            makeCanDismiss()
        }
    }
}