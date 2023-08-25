package xcj.app.appsets.ui.nonecompose

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import xcj.app.appsets.databinding.ActivityDialogBinding
import xcj.app.appsets.ktx.animateAlpha
import xcj.app.appsets.util.BlurAlgorithm
import xcj.app.appsets.util.RenderScriptBlur
import java.io.File
import java.io.FileInputStream

class JumpFromOtherAppLayerDialogActivity : AppCompatActivity() {
    lateinit var binding:ActivityDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    lateinit var blurAlgorithm: BlurAlgorithm
    private fun initView() {
        blurAlgorithm = RenderScriptBlur(this)
        binding.apply {
            val hello = intent.getStringExtra("hello")
            tv.text = hello
            val uri = Uri.parse("content://xcj.appsets.provider/bitmap")
            val id = intent.getStringExtra("bitmapId")
            setResult(RESULT_OK, Intent().apply {
                putExtra("id", id)
            })
            root.post {
                contentResolver.query(uri, arrayOf("id"), null, arrayOf(id), null)?.use { cursor ->
                    if (cursor.count == 1) {
                        Log.e("JumpFromOtherAppLayerDialogActivity", "cursor.count==1")
                        while (cursor.moveToNext()) {
                            val index = cursor.getColumnIndexOrThrow("path")
                            val path = cursor.getString(index)
                            /*val parse =
                                Uri.parse("content://xcj.appsets.provider/bitmap?path=${path}")
                            val bitmapDrawable =
                                BitmapFactory.decodeFile().toDrawable(resources)*/
                            //val file = File(path)
                            val uri1 = Uri.parse("content://xcj.appsets.provider/bitmap/#$path")
                            val fileDescriptor = contentResolver.openFileDescriptor(uri1, "r", null)
                            Log.e(
                                "JumpFromOtherAppLayerDialogActivity",
                                "bitmap parcelFileDescriptor:${fileDescriptor},filed:${fileDescriptor?.fileDescriptor}"
                            )
                            File("ss").inputStream()
                            val inputStream = FileInputStream(fileDescriptor?.fileDescriptor)
                            val bitmap1 =
                                BitmapFactory.decodeStream(inputStream)
                            val blurBitmap = blurAlgorithm.blur(bitmap1, 20f)?.toDrawable(resources)
                            ivBg.setImageDrawable(blurBitmap)
                            ivBg.animateAlpha(800)
                        }
                    }
                }
            }

        }
    }
}
