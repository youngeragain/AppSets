package xcj.app.appsets.ui.nonecompose.ui.login

import android.view.View
import androidx.lifecycle.MutableLiveData
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel

class LoginFragmentVM : BaseViewModel() {
    val account:MutableLiveData<String> =  MutableLiveData()
    val password:MutableLiveData<String> =  MutableLiveData()
    fun login(view: View){

    }
}