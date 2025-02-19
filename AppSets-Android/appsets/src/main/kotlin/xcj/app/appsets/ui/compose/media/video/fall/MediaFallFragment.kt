@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.media.video.fall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import xcj.app.starter.android.ui.base.DesignFragment


class MediaFallFragment : DesignFragment() {

    companion object {
        private const val TAG = "MediaFallFragment"
    }

    private val activityViewModel by activityViewModels<MediaFallViewModel>()

    private val viewModel by viewModels<MediaFallFragmentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
            }

            override fun onStart(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
            }

            override fun onResume(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
            }

            override fun onPause(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
            }

            override fun onStop(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
            }

            override fun onDestroy(owner: LifecycleOwner) {
                viewModel.lifecycleState.value = owner.lifecycle.currentState
                owner.lifecycle.removeObserver(this)
            }
        })
        viewModel.onAttach(this)
    }

    @UnstableApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MediaFallFragmentContent(
                    activityViewModel,
                    viewModel
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun getLoadedVideoMediaContent(): VideoMediaContent? {
        return viewModel.getCurrentVideoMediaContent()
    }

    fun setLoadedVideoMediaContent(videoMediaContent: VideoMediaContent) {
        viewModel.setCurrentVideoMediaContent(videoMediaContent)
    }
}