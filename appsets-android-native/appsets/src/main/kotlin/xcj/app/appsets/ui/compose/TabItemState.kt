package xcj.app.appsets.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

data class TabItemState(
    val type:String,
    var iconRes: Int,
    var name:String="tabName",
    var description:String?="tabDescription",
    var isSelect: MutableState<Boolean> = mutableStateOf(false)

){

    val transFormTextColor: Color
    @Composable get(){
        return if (isSelect.value) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }
    }
    val transFormIconTintColor:Color
    @Composable get(){
        return if (isSelect.value) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }
    }
}