package xcj.app.appsets.ui.nonecompose.ui.outside

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import xcj.app.appsets.databinding.ActivityOutSideBinding
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory


class OutSideViewModel : BaseViewModel() {

}


class OutSideActivity :
    BaseActivity<ActivityOutSideBinding, OutSideViewModel, BaseViewModelFactory<OutSideViewModel>>() {

    override fun createBinding(): ActivityOutSideBinding? {
        return ActivityOutSideBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): OutSideViewModel? {
        return ViewModelProvider(this)[OutSideViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        /* WindowCompat.setDecorFitsSystemWindows(window, false)*/
        super.onCreate(savedInstanceState)
    }
}