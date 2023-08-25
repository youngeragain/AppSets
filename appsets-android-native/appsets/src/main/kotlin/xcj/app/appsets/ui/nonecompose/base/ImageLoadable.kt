package xcj.app.appsets.ui.nonecompose.base

import androidx.appcompat.widget.AppCompatImageView

interface  ImageLoadable{
    fun <T:Any> load(appCompatImageView: AppCompatImageView, t:T?)
}