package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

open class DataLoadStateContainer<D> {
    var page: Int = 1
    var pageSize: Int = 15
    var lastScreensSize: Int = -1
    val isRequesting: MutableState<Boolean> = mutableStateOf(false)
    val screens: MutableList<D> = mutableStateListOf()
}