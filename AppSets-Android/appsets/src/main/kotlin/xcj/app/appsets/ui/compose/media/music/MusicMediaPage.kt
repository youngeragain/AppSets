/*
package xcj.app.appsets.ui.compose.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.start.ComponentAudioPlayerDefaultActionButtons
import xcj.app.appsets.ui.compose.start.ComponentAudioPlayerMiniCardContent
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "MusicMediaPage"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicMediaPage(
    onSnapShotStateClick: (Any, Any?) -> Unit
) {
    HideNavBarWhenOnLaunch()
    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
    val musicMediaContentList by mediaRemoteExoUseCase.serverMusicMediaContentList
    val indexWhenPlay = if (mediaRemoteExoUseCase.currentPlaybackMusicMediaContent != null) {
        musicMediaContentList?.indexOf(mediaRemoteExoUseCase.currentPlaybackMusicMediaContent)
    } else {
        0
    } ?: 0
    val pagerState =
        rememberPagerState(initialPage = indexWhenPlay) { musicMediaContentList?.size ?: 1 }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { index -> index }
    ) { pageIndex ->
        var isPreparingState by remember {
            mutableStateOf(true)
        }
        var mPlaying by remember {
            mutableStateOf(false)
        }
        val audioPlayerState by mediaRemoteExoUseCase.audioPlayerState
        LaunchedEffect(key1 = pageIndex) {
            PurpleLogger.current.e(
                TAG,
                "pagerState.currentPage:${pagerState.currentPage}, pagerState.targetPage:${pagerState.targetPage}, pagerState.settledPage:${pagerState.settledPage}, pageIndex:${pageIndex}"
            )
            if (pagerState.currentPage != pageIndex) {
                mediaRemoteExoUseCase.toPrepareState()
                delay(1000)
            }
            if (pagerState.currentPage == pageIndex) {
                val mediaContent = musicMediaContentList?.getOrNull(pageIndex)
                mediaRemoteExoUseCase.playMusicMediaContent(mediaContent)
            }
            PurpleLogger.current.e(
                TAG,
                "after 1000ms pagerState.currentPage:${pagerState.currentPage}, pageIndex:${pageIndex}"
            )
        }
        SideEffect {
            if (pagerState.currentPage == pageIndex) {
                mPlaying = mediaRemoteExoUseCase.isPlaying
                isPreparingState = mediaRemoteExoUseCase.isPreparingState()
            }
        }
        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            ComponentAudioPlayerMiniCardContent(
                audioPlayerState = audioPlayerState,
                isPlaying = mPlaying,
                isPreparingState = isPreparingState,
                seekRequest = { percentage ->
                    mediaRemoteExoUseCase.requestSeekTo((audioPlayerState.durationRawValue * percentage).toLong())
                },
                actionButtonsContent = {
                    ComponentAudioPlayerDefaultActionButtons(
                        isPlaying = mPlaying
                    ) {
                        mPlaying = !mPlaying
                        if (!mPlaying) {
                            mediaRemoteExoUseCase.requestPause()
                        } else {
                            mediaRemoteExoUseCase.requestPlay()
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.navigationBars))
        }
    }
}*/
