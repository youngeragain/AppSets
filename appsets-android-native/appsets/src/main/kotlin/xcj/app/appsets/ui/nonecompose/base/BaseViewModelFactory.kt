package xcj.app.appsets.ui.nonecompose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseViewModelFactory<VM : CommonViewModel> : ViewModelProvider.Factory {

    abstract fun create(): VM

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val createdVm = create()
        if (BaseViewModel::class.java.isAssignableFrom(createdVm::class.java) ||
            BaseAndroidViewModel::class.java.isAssignableFrom(createdVm::class.java)
        ) {
            return createdVm as T
        } else {
            throw Exception()
        }
    }
}