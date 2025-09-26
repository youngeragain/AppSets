package xcj.app.appsets.ui.model

import androidx.annotation.StringRes

interface TipsProvider {
    @get:StringRes
    val tips: Int?

    @get:StringRes
    val subTips: Int?
}