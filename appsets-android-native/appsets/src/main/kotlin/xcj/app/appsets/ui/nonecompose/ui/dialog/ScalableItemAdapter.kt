package xcj.app.appsets.ui.nonecompose.ui.dialog

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toFile
import coil.size.Size
import coil.size.pxOrElse
import coil.transform.Transformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import xcj.app.appsets.R
import xcj.app.appsets.databinding.ItemFileEntityListStyleBinding
import xcj.app.appsets.databinding.ItemRoundedRectangleImgAndText1Binding
import xcj.app.appsets.databinding.ItemRoundedRectangleImgAndTextBinding
import xcj.app.appsets.databinding.ItemTakePicBinding
import xcj.app.appsets.databinding.ItemVideoListStyleBinding
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ui.nonecompose.base.BaseAdapter
import xcj.app.appsets.ui.nonecompose.base.MyViewHolder
import xcj.app.core.android.ktx.dp
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

/**
 * 有伸缩性的的图片展示adapter, 即图片数量可以修改，包含一个添加图片的按钮在最后。
 */
class ScalableItemAdapter(val id: String? = null) :
    BaseAdapter<ScalableItemState>() {
    private var SIZE = Short.MAX_VALUE.toInt()
    private fun convertDeleteAblePic(
        holder: MyViewHolder<ScalableItemState>,
        itemData: ScalableItemState
    ) {
        (holder.binding as? ItemRoundedRectangleImgAndTextBinding)?.apply {
            load(
                ivImg, if (itemData.any !is Uri) {
                    (itemData.any as MediaStoreDataUriWrapper).uri
                } else itemData.any
            )
            tvDelete.visibility = if (!itemData.showDelete) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun convertSelectablePic(
        holder: MyViewHolder<ScalableItemState>,
        itemData: ScalableItemState
    ) {
        (holder.binding as? ItemRoundedRectangleImgAndText1Binding)?.apply {
            load(
                ivImg, if (itemData.any !is Uri) {
                    (itemData.any as MediaStoreDataUriWrapper).uri
                } else itemData.any
            )
            viewMask.alpha = if (itemData.selected) {
                1f
            } else {
                0f
            }
        }
    }

    fun tvDeleteClick(item: ScalableItemState) {
        val lastSize = data.size
        val lastType = data.last().type
        var deletedIndex = -1
        for (i in data.size - 1 downTo 0) {
            if (data[i] == item) {
                deletedIndex = i
                break
            }
        }
        if (deletedIndex != -1) {
            data.removeAt(deletedIndex)
        }
        if (lastSize == SIZE && lastType == TYPE_TAKE_A_PIC) {
            notifyItemRemoved(deletedIndex)
        } else if ((lastSize == SIZE && lastType == TYPE_PIC_DELETEABLE)) {
            data.add(ScalableItemState(type = TYPE_TAKE_A_PIC))
            notifyItemRemoved(deletedIndex)
            notifyItemInserted(SIZE)
        } else if (lastSize < SIZE && lastType == TYPE_TAKE_A_PIC) {
            notifyItemRemoved(deletedIndex)
        }
    }


    fun updateUI(list: List<ScalableItemState>, shouldClean: Boolean = false) {
        if (shouldClean) {
            if (data.isNotEmpty())
                data.clear()
            data.addAll(list)
            if (list.size < SIZE) {
                data.add(ScalableItemState(type = TYPE_TAKE_A_PIC))
            }
            notifyDataSetChanged()
            return
        }
        if (data.isEmpty()) {
            data.addAll(list)
            notifyDataSetChanged()
            return
        }
        val lastSize = data.size
        if (
            lastSize == SIZE &&
            data.last().type == TYPE_TAKE_A_PIC &&
            list.size == 1
        ) {
            data.removeLast()
            data.add(list[0])
            notifyItemChanged(lastSize - 1)
        } else {
            data.addAll(lastSize - 1, list)
            if (data.last().type == TYPE_TAKE_A_PIC) {
                if ((list.size + lastSize - 1) == SIZE)
                    data.removeLast()
            }
            notifyDataSetChanged()
        }
    }

    val selectedItems by lazy { mutableListOf<ScalableItemState>() }

    fun flipItemState(position:Int, maxSelectCount:Int){
        val picsAndTakePicData = data[position]
        try {
            val file = (picsAndTakePicData.any as? Uri)?.toFile()
            if (file?.exists() == true && file.length() > MAX_FILE_SIZE) {
                "file size is too big!"
                return
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        var oldChangePosition = -1
        if (!picsAndTakePicData.selected) {
            if (maxSelectCount == 1) {
                if (selectedItems.size == 1) {
                    val removed = selectedItems.removeAt(0)
                    removed.selected = false
                    oldChangePosition = data.indexOf(removed)
                }
            } else {
                if (selectedItems.size + 1 > maxSelectCount)
                    return
            }
            picsAndTakePicData.selected = true
        } else {
            picsAndTakePicData.selected = false
        }
        if (picsAndTakePicData.selected) {
            if (!selectedItems.contains(picsAndTakePicData))
                selectedItems.add(picsAndTakePicData)
        } else {
            if (selectedItems.contains(picsAndTakePicData)) {
                selectedItems.remove(picsAndTakePicData)
            }
        }
        val changePosition = position
        /*for(i in data.indices){
            if (data[i] == picsAndTakePicData) {
                changePosition = i
                break
            }
        }*/
        if (oldChangePosition != -1) {
            notifyItemChanged(oldChangePosition, "CHANGE_FOR_SELECT")
        }
        notifyItemChanged(changePosition, "CHANGE_FOR_SELECT")

    }

    override fun bind(holder: MyViewHolder<ScalableItemState>, position: Int) {
        if (data.size == 0) {
            return
        }
        val itemData = data[position]
        when (itemData.type) {
            TYPE_PIC_DELETEABLE -> convertDeleteAblePic(holder, itemData)
            TYPE_PIC_SELECTABLE -> convertSelectablePic(holder, itemData)
            TYPE_VIDEO_SELECTABLE -> convertSelectableVideo(holder, itemData)
            TYPE_AUDIO_SELECTABLE -> convertSelectableAudio(holder, itemData)
            TYPE_FILE_SELECTABLE -> convertSelectableFile(holder, itemData)
        }
    }

    private fun convertSelectableFile(
        holder: MyViewHolder<ScalableItemState>,
        itemData: ScalableItemState
    ) {
        val mediaStoreDataUriWrapper = itemData.any as MediaStoreDataUriWrapper
        (holder.binding as ItemFileEntityListStyleBinding).apply {
            tvFileEntityName.text = mediaStoreDataUriWrapper.name
            tvFileEntitySize.text = mediaStoreDataUriWrapper.sizeReadable
            load(ivFileEntityIcon, R.drawable.outline_insert_drive_file_24)
        }
    }

    private fun convertSelectableAudio(
        holder: MyViewHolder<ScalableItemState>,
        itemData: ScalableItemState
    ) {
        val mediaStoreDataUriWrapper = itemData.any as MediaStoreDataUriWrapper
        (holder.binding as ItemFileEntityListStyleBinding).apply {
            tvFileEntityName.text = mediaStoreDataUriWrapper.name
            tvFileEntitySize.text = mediaStoreDataUriWrapper.sizeReadable
            load(ivFileEntityIcon, R.drawable.outline_audiotrack_24)
        }
    }

    private fun convertSelectableVideo(
        holder: MyViewHolder<ScalableItemState>,
        itemData: ScalableItemState
    ) {
        val mediaStoreDataUriWrapper = itemData.any as MediaStoreDataUriWrapper
        (holder.binding as ItemVideoListStyleBinding).apply {
            tvFileEntityName.text = mediaStoreDataUriWrapper.name
            tvFileEntitySize.text = mediaStoreDataUriWrapper.sizeReadable
            load(
                ivFileEntityIcon, if (itemData.any !is Uri) {
                    (itemData.any as MediaStoreDataUriWrapper).thumbnail
                } else itemData.any
            )
        }
    }

    override fun bind(
        holder: MyViewHolder<ScalableItemState>,
        position: Int,
        payloads: List<Any>
    ) {
        if (data.size == 0) {
            return
        }
        val itemData = data[position]
        if(payloads.contains("CHANGE_FOR_SELECT")) {
            fun commonAnimate(viewOfCurrentSelectCount: AppCompatTextView, viewOfMask: View) {
                val alpha = if (itemData.selected) {
                    1f
                } else {
                    0f
                }
                if (itemData.selected) {
                    viewOfCurrentSelectCount.animate().alpha(1f).setDuration(150).withStartAction {
                        viewOfCurrentSelectCount.text = selectedItems.size.toString()
                    }.withEndAction {
                        viewOfCurrentSelectCount.animate().alpha(0f).setDuration(150).start()
                    }.start()
                }
                viewOfMask.animate().alpha(alpha).setDuration(300).start()
            }
            when (holder.binding) {
                is ItemRoundedRectangleImgAndText1Binding -> {
                    commonAnimate(holder.binding.viewCurrentSize, holder.binding.viewMask)
                }

                is ItemFileEntityListStyleBinding -> {
                    commonAnimate(holder.binding.viewCurrentSize, holder.binding.viewMask)
                }

                is ItemVideoListStyleBinding -> {
                    commonAnimate(holder.binding.viewCurrentSize, holder.binding.viewMask)
                }
            }
        }else
            super.bind(holder, position, payloads)
    }
    override fun initItems(itemsTypeAndBindingClass: MutableMap<Int, KClass<*>>) {
        itemsTypeAndBindingClass.apply {
            put(TYPE_PIC_DELETEABLE, ItemRoundedRectangleImgAndTextBinding::class)
            put(TYPE_PIC_SELECTABLE, ItemRoundedRectangleImgAndText1Binding::class)
            put(TYPE_TAKE_A_PIC, ItemTakePicBinding::class)
            put(TYPE_AUDIO_SELECTABLE, ItemFileEntityListStyleBinding::class)
            put(TYPE_VIDEO_SELECTABLE, ItemVideoListStyleBinding::class)
            put(TYPE_FILE_SELECTABLE, ItemFileEntityListStyleBinding::class)
        }
    }
    private val radius = 8.dp()
    override fun <T : Any> load(appCompatImageView: AppCompatImageView, t: T?) {
        Glide.with(appCompatImageView)
            .load(t)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(appCompatImageView)
    }

    fun initData(listOf: List<ScalableItemState>) {
        if (data.isNotEmpty())
            data.clear()
        data.addAll(listOf)
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_PIC_DELETEABLE = 0
        const val TYPE_PIC_SELECTABLE = 1
        const val TYPE_TAKE_A_PIC = 2
        const val TYPE_AUDIO_SELECTABLE = 3
        const val TYPE_VIDEO_SELECTABLE = 4
        const val TYPE_FILE_SELECTABLE = 5
        const val TYPE_UNDEFINED = 6

        const val MAX_FILE_SIZE = 20_971_520
    }
}

class CoilCenterCrop() : Transformation {
    override val cacheKey: String
        get() = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        return centerCrop(input, size.width.pxOrElse { 0 }, size.height.pxOrElse { 0 })
    }

    companion object {
        fun centerCrop(
            inBitmap: Bitmap, width: Int, height: Int
        ): Bitmap {
            if (inBitmap.width == width && inBitmap.height == height) {
                return inBitmap
            }
            // From ImageView/Bitmap.createScaledBitmap.
            val scale: Float
            val dx: Float
            val dy: Float
            val m = Matrix()
            if (inBitmap.width * height > width * inBitmap.height) {
                scale = height.toFloat() / inBitmap.height.toFloat()
                dx = (width - inBitmap.width * scale) * 0.5f
                dy = 0f
            } else {
                scale = width.toFloat() / inBitmap.width.toFloat()
                dx = 0f
                dy = (height - inBitmap.height * scale) * 0.5f
            }
            m.setScale(scale, scale)
            m.postTranslate((dx + 0.5f).toInt().toFloat(), (dy + 0.5f).toInt().toFloat())
            val result = Bitmap.createBitmap(width, height, getNonNullConfig(inBitmap))
            // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
            setAlpha(inBitmap, result)
            applyMatrix(inBitmap, result, m)
            return result
        }

        private val BITMAP_DRAWABLE_LOCK = ReentrantLock()
        const val PAINT_FLAGS = Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG
        private val DEFAULT_PAINT = Paint(PAINT_FLAGS)
        private fun applyMatrix(
            inBitmap: Bitmap, targetBitmap: Bitmap, matrix: Matrix
        ) {
            BITMAP_DRAWABLE_LOCK.lock()
            try {
                val canvas = Canvas(targetBitmap)
                canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT)
                clear(canvas)
            } finally {
                BITMAP_DRAWABLE_LOCK.unlock()
            }
        }

        private fun clear(canvas: Canvas) {
            canvas.setBitmap(null)
        }

        fun setAlpha(inBitmap: Bitmap, outBitmap: Bitmap) {
            outBitmap.setHasAlpha(inBitmap.hasAlpha())
        }

        private fun getNonNullConfig(bitmap: Bitmap): Bitmap.Config {
            return bitmap.config
        }
    }
}