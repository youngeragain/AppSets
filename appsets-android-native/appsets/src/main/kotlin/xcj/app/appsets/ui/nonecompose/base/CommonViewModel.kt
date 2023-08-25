package xcj.app.appsets.ui.nonecompose.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel


interface CommonViewModel

abstract class BaseViewModel : ViewModel(), CommonViewModel

abstract class BaseAndroidViewModel(
    application: Application
) : AndroidViewModel(application), CommonViewModel
