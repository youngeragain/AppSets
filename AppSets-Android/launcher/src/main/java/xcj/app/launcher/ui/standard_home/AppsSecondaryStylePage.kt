package xcj.app.launcher.ui.standard_home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.starter.android.AppDefinition

@Composable
fun AppsSecondaryStylePage(
    onAppClick: (AppDefinition) -> Unit
) {
    var searchTextField by remember {
        mutableStateOf(TextFieldValue())
    }

    var isShowAlphabetChoosePanel by remember {
        mutableStateOf(false)
    }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val appDefinitionList by viewModel.apps
    val context = LocalContext.current
    val appsWithAlphabet by remember {
        derivedStateOf {
            val models = mutableListOf<Any>()
            appDefinitionList
                .groupBy { app ->
                    app.name?.getOrNull(0)?.toString()?.uppercase() ?: "*"
                }
                .toSortedMap()
                .forEach { (appAlphabet, apps) ->
                    if (searchTextField.text.isNotEmpty() || searchTextField.text.isNotBlank()) {
                        val filteredApps =
                            apps.filter { app ->
                                app.name?.contains(
                                    searchTextField.text,
                                    true
                                ) == true
                            }
                        if (filteredApps.isNotEmpty()) {
                            models.add(appAlphabet)
                            models.addAll(filteredApps)
                        }
                    } else {
                        models.add(appAlphabet)
                        models.addAll(apps)
                    }
                }
            models
        }
    }
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isShowAlphabetChoosePanel,
            label = "show_alphabet_choose_panel_animate",
            transitionSpec = {
                (fadeIn(animationSpec = tween(350)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(350)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(200)) + scaleOut(
                            targetScale = 0.9f,
                            animationSpec = tween(200)
                        )
                    )
            },
            contentAlignment = Alignment.Center
        ) { isShow ->
            if (!isShow) {
                AppsSecondaryStylePageBaseLayer(
                    appsWithAlphabet = appsWithAlphabet,
                    lazyListState = lazyListState,
                    searchTextField = searchTextField,
                    onSearchTextFieldValueChanged = {
                        searchTextField = it
                    },
                    onAlphabetClick = {
                        isShowAlphabetChoosePanel = true
                    },
                    onAppClick = onAppClick
                )

            } else {
                AppsSecondaryStylePageAlphabetChoosePanelLayer(
                    onAlphabetClick = { alphabet ->
                        isShowAlphabetChoosePanel = false
                        if (alphabet.isNullOrEmpty()) {
                            return@AppsSecondaryStylePageAlphabetChoosePanelLayer
                        }
                        scope.launch {
                            delay(100)
                            val alphabetIndex = appsWithAlphabet.indexOfFirst {
                                if (it is String) {
                                    it.lowercase() == alphabet.lowercase()
                                } else {
                                    false
                                }
                            }
                            if (alphabetIndex != -1) {
                                lazyListState.animateScrollToItem(alphabetIndex)
                            }
                        }
                    }
                )
            }
        }
    }
}
