package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.starter.android.util.UriProvider

data class GroupInfoForCreate(
    val name: MutableState<String> = mutableStateOf(""),
    val membersCount: MutableState<String> = mutableStateOf(""),
    val isPublic: MutableState<Boolean> = mutableStateOf(false),
    val introduction: MutableState<String> = mutableStateOf(""),
    val iconUriProvider: MutableState<UriProvider?> = mutableStateOf(null)
)