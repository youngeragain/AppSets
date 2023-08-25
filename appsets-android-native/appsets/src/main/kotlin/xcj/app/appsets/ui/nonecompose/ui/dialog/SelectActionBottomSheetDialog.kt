package xcj.app.appsets.ui.nonecompose.ui.dialog

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.databinding.DialogSelectActionBinding
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.getSystemFileUris
import xcj.app.appsets.ktx.gone
import xcj.app.appsets.ktx.post
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ui.nonecompose.base.BaseBottomSheetDialog
import xcj.app.appsets.ui.nonecompose.base.BaseFragment
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.purple_module.ModuleConstant


/**
 * 选择图片
 */
class SelectActionBottomSheetDialog :
    BaseBottomSheetDialog<SelectActionVM, DialogSelectActionBinding>() {
    private var fragments: List<ActionTypeFragment>? = null
    private val tags = mutableListOf("open_camera", "location", "picture", "video", "audio", "file")
    private val selectedFileUris: List<Uri>? by lazy {
        arguments?.getStringArray("selected_uri_list")?.mapNotNull {
            it.toUri()
        }
    }

    override fun createViewModel(): SelectActionVM {
        return ViewModelProvider(this)[SelectActionVM::class.java]
    }

    override fun createBinding(): DialogSelectActionBinding? {
        return DialogSelectActionBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val actions = arguments?.getStringArray("actions")
        var title = "选择一项操作"
        if (!actions.isNullOrEmpty()) {
            title = if (actions.size == 1) {
                when (actions[0]) {
                    "open_camera" -> "使用相机拍摄"
                    "location" -> "选择位置信息"
                    "picture" -> "选择图片"
                    "video" -> "选择视频"
                    "audio" -> "选择音频"
                    "file" -> "选择文件"
                    else -> "选择一项操作"
                }
            } else {
                "选择一项操作"
            }
            val tagsIterator = tags.iterator()
            while (tagsIterator.hasNext()) {
                val element = tagsIterator.next()
                if (!actions.contains(element)) {
                    tagsIterator.remove()
                }
            }
        }
        baseBinding.tvTitle.text = title
        fragments = tags.map { actionType ->
            ActionTypeFragment().apply {
                arguments = Bundle(this@SelectActionBottomSheetDialog.arguments).apply {
                    putString("action_type", actionType)
                }
                mSelectionChangeListener =
                    ActionTypeFragment.SelectionChangeListener { selectedCount ->
                        this@SelectActionBottomSheetDialog.binding?.tvConfirm?.isSelected =
                            selectedCount > 0
                        this@SelectActionBottomSheetDialog.binding?.tvConfirm?.isEnabled =
                            selectedCount > 0
                    }

            }
        }
        val fragmentStateAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragments?.size ?: 0
            }

            override fun createFragment(position: Int): Fragment {
                return fragments?.get(position) ?: throw Exception()
            }
        }
        binding?.apply {
            viewpager2.adapter = fragmentStateAdapter
            viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val tagSelected = tags[position]
                    fragments?.get(position)?.shouldEnableConfirmButton()
                        ?.let { shouldEnableConfirmButton ->
                            tvConfirm.isSelected = shouldEnableConfirmButton
                            tvConfirm.isEnabled = shouldEnableConfirmButton
                        }
                    ivOpenCamera.isSelected = tagSelected == ivOpenCamera.tag
                    tvLocation.isSelected = tagSelected == tvLocation.tag
                    tvPicture.isSelected = tagSelected == tvPicture.tag
                    tvVideo.isSelected = tagSelected == tvVideo.tag
                    tvAudio.isSelected = tagSelected == tvAudio.tag
                    tvFile.isSelected = tagSelected == tvFile.tag
                }
            })
            val listener1 = View.OnClickListener {
                if (it.id == tvConfirm.id) {
                    fragments?.get(viewpager2.currentItem)?.getSelectItems()?.toMutableList()
                        ?.let { uriWrappers ->
                            if (uriWrappers.isEmpty()) {
                                "请选择内容".toast()
                            } else {
                                if (!selectedFileUris.isNullOrEmpty())
                                    removeDuplicateAllByPlatform(uriWrappers, selectedFileUris)
                                ModuleConstant.MSG_DELIVERY_KEY_SELECTOR_ITEM_SELECTED.post(tags[viewpager2.currentItem] to uriWrappers)
                                dismiss()
                            }
                        }
                    return@OnClickListener
                }
                if (it.id == tvShowContent.id) {
                    if (clShowContentContainer.visibility != View.GONE)
                        clShowContentContainer.animate().alpha(0f).setDuration(100).withEndAction {
                            clShowContentContainer.visibility = View.GONE
                        }.start()
                    return@OnClickListener
                }
                ivOpenCamera.isSelected = it == ivOpenCamera
                tvLocation.isSelected = it == tvLocation
                tvPicture.isSelected = it == tvPicture
                tvVideo.isSelected = it == tvVideo
                tvAudio.isSelected = it == tvAudio
                tvFile.isSelected = it == tvFile
                val indexOf = tags.indexOf(it.tag)
                if (indexOf != -1)
                    viewpager2.currentItem = indexOf
            }
            if (tags.size == 1) {
                horizontalScrollView.gone()
            } else {
                if (tags.contains("open_camera")) {
                    ivOpenCamera.setOnClickListener(listener1)
                } else {
                    ivOpenCamera.gone()
                }
                if (tags.contains("location")) {
                    tvLocation.setOnClickListener(listener1)
                } else {
                    tvLocation.gone()
                }
                if (tags.contains("picture")) {
                    tvPicture.setOnClickListener(listener1)
                } else {
                    tvPicture.gone()
                }
                if (tags.contains("video")) {
                    tvVideo.setOnClickListener(listener1)
                } else {
                    tvVideo.gone()
                }
                if (tags.contains("audio")) {
                    tvAudio.setOnClickListener(listener1)
                } else {
                    tvAudio.gone()
                }
                if (tags.contains("file")) {
                    tvFile.setOnClickListener(listener1)
                } else {
                    tvFile.gone()
                }
            }
            tvConfirm.setOnClickListener(listener1)
            tvShowContent.setOnClickListener(listener1)
        }
    }

    private fun removeDuplicateAllByPlatform(
        rawList: MutableList<MediaStoreDataUriWrapper>,
        items: List<Uri>?
    ) {
        items ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rawList.removeIf { wrapper ->
                items.firstOrNull { it.path == wrapper.uri.path } != null
            }
        } else {
            for (item1 in rawList) {
                for (item2 in items) {
                    if (item1.uri.path == item2.path) {
                        rawList.remove(item1)
                        break
                    }
                }
            }
        }
    }

}

class ActionTypeViewModel : BaseViewModel() {
    var page: Int = 1
    private var defaultPageSize: Int = 32


    /**
     * 标记即将选择的图片
     */
    var flag: Int? = null

    /**
     * 已经选择过的图片
     */
    var selectedFileUris: List<Uri> = mutableListOf()

    /**
     * 最大可选数
     */
    var maxSelectCount: Int = Byte.MAX_VALUE.toInt()

    /**
     * 分页加载图片时的中间对象
     */
    val fileUris: MutableLiveData<List<ScalableItemState>> by lazy { MutableLiveData() }

    var mediaStoreType: Class<*>? = null

    private var loading: Boolean = false

    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever

    fun load(context: Context, first: Boolean = false) {
        if (mediaStoreType == null)
            return
        if (loading)
            return
        loading = true
        if (first) {
            page = 1
        } else {
            if ((fileUris.value?.size ?: 0) < defaultPageSize) {
                return
            } else {
                page++
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            getUris(context, page, defaultPageSize)
        }
    }

    private suspend fun getUris(context: Context, page: Int, pageSize: Int) {
        val fromIndex = ((page - 1) * pageSize)
        //checkRange()
        val systemFileUris =
            context.getSystemFileUris(mediaStoreType, 1, fromIndex, pageSize)
        if (systemFileUris.isNullOrEmpty()) {
            Log.e("ActionTypeViewModel", "empty files")
            return
        }
        val adapterItemType = when (mediaStoreType) {
            MediaStore.Images::class.java -> ScalableItemAdapter.TYPE_PIC_SELECTABLE
            MediaStore.Video::class.java -> ScalableItemAdapter.TYPE_VIDEO_SELECTABLE
            MediaStore.Audio::class.java -> ScalableItemAdapter.TYPE_AUDIO_SELECTABLE
            MediaStore.Files::class.java -> ScalableItemAdapter.TYPE_FILE_SELECTABLE
            else -> ScalableItemAdapter.TYPE_UNDEFINED
        }
        if (mediaStoreType == MediaStore.Video::class.java) {
            if (!::mediaMetadataRetriever.isInitialized) {
                mediaMetadataRetriever = MediaMetadataRetriever()
            }
        }
        val pagedSystemPhotosFileUris = systemFileUris.map {
            val selected =
                selectedFileUris.find { selectedFileUri -> selectedFileUri.path == it.uri.path } != null
            val scalableItemState = ScalableItemState(
                any = it,
                type = adapterItemType,
                selected = selected
            )
            if (mediaStoreType == MediaStore.Video::class.java) {
                kotlin.runCatching {
                    mediaMetadataRetriever.setDataSource(context, it.uri)
                    it.thumbnail = mediaMetadataRetriever.frameAtTime
                }
            }
            scalableItemState
        }
        withContext(Dispatchers.Main) {
            fileUris.value = pagedSystemPhotosFileUris
        }
        loading = false
    }

    override fun onCleared() {
        super.onCleared()
        if (::mediaMetadataRetriever.isInitialized) {
            mediaMetadataRetriever.release()
        }
    }


}

class ActionTypeFragment :
    BaseFragment<ViewDataBinding, ActionTypeViewModel, BaseViewModelFactory<ActionTypeViewModel>>() {

    fun interface SelectionChangeListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    var mSelectionChangeListener: SelectionChangeListener? = null

    private val actionType: String? by lazy { arguments?.getString("action_type") }
    private val maxSelectCount: Int by lazy { arguments?.getInt("max_select_count", 1) ?: 1 }
    private lateinit var mAdapter: ScalableItemAdapter
    private lateinit var mRecyclerView: RecyclerView
    override fun createViewModel(): ActionTypeViewModel? {
        return ViewModelProvider(this)[ActionTypeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.mediaStoreType = when (actionType) {
            "picture" -> {
                MediaStore.Images::class.java
            }

            "video" -> {
                MediaStore.Video::class.java
            }

            "audio" -> {
                MediaStore.Audio::class.java
            }

            "file" -> {
                MediaStore.Files::class.java
            }

            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        when (actionType) {
            "open_camera" -> {
                return TextView(requireContext()).apply {
                    text = "打开相机拍摄"
                }
            }

            "location" -> {
                return TextView(requireContext()).apply {
                    text = "选择位置信息"
                }
            }

            else -> {
                return RecyclerView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }.also {
                    mRecyclerView = it
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        createObserver()
    }

    private fun initView() {
        when (actionType) {
            "open_camera" -> {

            }

            "location" -> {

            }

            else -> {
                mRecyclerView.apply {
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            if (dy <= 0)
                                return
                            if (!recyclerView.canScrollVertically(1)) {
                                viewModel?.load(requireContext(), false)
                            }
                        }
                    })
                    layoutManager = if (actionType == "picture") {
                        GridLayoutManager(requireContext(), 3)
                    } else {
                        LinearLayoutManager(requireContext())
                    }
                    mAdapter = ScalableItemAdapter(actionType).apply {
                        itemClickListener = { _, position ->
                            flipItemState(position, maxSelectCount)
                            mSelectionChangeListener?.onSelectionChanged(this.selectedItems.size)
                        }
                    }
                    adapter = mAdapter
                }
                viewModel?.load(requireContext(), true)
            }
        }
    }

    private fun createObserver() {
        when (actionType) {
            "open_camera" -> {

            }

            "location" -> {

            }

            else -> {
                viewModel?.fileUris?.observe(viewLifecycleOwner, Observer { dataList ->
                    if (dataList.isNullOrEmpty())
                        return@Observer
                    Log.e("ActionTypeFragment", "dataList:Size:${dataList.size}")
                    if (viewModel?.page == 1) {
                        mAdapter.data.addAll(dataList)
                        mAdapter.notifyDataSetChanged()
                    } else {
                        val lastSize = mAdapter.data.size
                        mAdapter.data.addAll(dataList)
                        mAdapter.notifyItemRangeInserted(lastSize, dataList.size)
                    }
                })
            }
        }
    }

    fun shouldEnableConfirmButton(): Boolean {
        when (actionType) {
            "open_camera" -> {
                return false
            }

            "location" -> {
                return false
            }

            else -> {
                if (!::mAdapter.isInitialized)
                    return false
                return mAdapter.selectedItems.size > 0
            }
        }
    }

    fun getSelectItems(): List<MediaStoreDataUriWrapper>? {
        when (actionType) {
            "open_camera" -> {
                return null
            }

            "location" -> {
                return null
            }

            else -> {
                if (mAdapter.selectedItems.isEmpty())
                    return null
                return mAdapter.selectedItems.mapNotNull { (it.any as? MediaStoreDataUriWrapper) }
            }
        }

    }
}