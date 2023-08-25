package xcj.app.appsets.ui.nonecompose.base

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController

interface Page<VM : CommonViewModel, VDB : ViewDataBinding?, VMF : BaseViewModelFactory<VM>?>{
    fun requireCtx(): Context
    fun requireVM(): VM?
    fun requireViewDataBinding(): VDB?
    fun initView()
    fun createObserver()
    fun getVMFactoryNeedsValue(): Set<Any>?
    suspend fun setOrCreateVM(vmClazz: Class<VM>?, vmfClazz: Class<VMF>?)
    fun createViewDataBinding(vdbClazz: Class<VDB>, parent: ViewGroup?): VDB?
    fun navController():NavController? = null
}

class PageHolder(private val fragment: BaseFragment<*, *, *>) :
    Page<CommonViewModel, ViewDataBinding, BaseViewModelFactory<CommonViewModel>> {
    override fun requireCtx(): Context {
        return fragment.requireContext()
    }

    override fun requireVM(): CommonViewModel? {
        return fragment.viewModel
    }

    override fun requireViewDataBinding(): ViewDataBinding? {
        return fragment.binding
    }

    override fun initView() {

    }


    override fun createObserver() {

    }

    override fun getVMFactoryNeedsValue(): Set<Any>? {
        return emptySet()
    }

    override suspend fun setOrCreateVM(
        vmClazz: Class<CommonViewModel>?,
        vmfClazz: Class<BaseViewModelFactory<CommonViewModel>>?
    ) {

    }

    override fun createViewDataBinding(
        vdbClazz: Class<ViewDataBinding>,
        parent: ViewGroup?
    ): ViewDataBinding? {
        return null
    }
}