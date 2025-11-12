package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.starter.android.util.UriProvider

data class UserInfoForCreate(
    val account: MutableState<String> = mutableStateOf(""),
    val password: MutableState<String> = mutableStateOf(""),
    val userName: MutableState<String> = mutableStateOf(""),
    val userIntroduction: MutableState<String> = mutableStateOf(""),
    val userTags: MutableState<String> = mutableStateOf(""),
    val userSex: MutableState<String> = mutableStateOf(""),
    val userAge: MutableState<String> = mutableStateOf(""),
    val userPhone: MutableState<String> = mutableStateOf(""),
    val userEmail: MutableState<String> = mutableStateOf(""),
    val userArea: MutableState<String> = mutableStateOf(""),
    val userAddress: MutableState<String> = mutableStateOf(""),
    val userWebsite: MutableState<String> = mutableStateOf(""),
    val userAvatarUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
)