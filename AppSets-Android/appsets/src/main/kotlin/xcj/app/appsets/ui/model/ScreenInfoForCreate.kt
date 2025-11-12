package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import xcj.app.starter.android.util.UriProvider

data class ScreenInfoForCreate(
    val isPublic: MutableState<Boolean> = mutableStateOf(true),
    val content: MutableState<String> = mutableStateOf(""),
    val associateTopics: MutableState<String> = mutableStateOf(""),
    val associatePeoples: MutableState<String> = mutableStateOf(""),
    val pictureUriProviders: MutableList<UriProvider> = mutableStateListOf(),
    val videoUriProviders: MutableList<UriProvider> = mutableStateListOf(),
    val addToMediaFall: MutableState<Boolean> = mutableStateOf(false),
)