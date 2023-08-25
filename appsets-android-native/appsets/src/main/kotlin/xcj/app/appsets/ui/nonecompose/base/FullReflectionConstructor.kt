package xcj.app.appsets.ui.nonecompose.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import xcj.app.core.android.toplevelfun.inflateViewDataBindingFromClass
import java.lang.reflect.ParameterizedType

class FullReflectionConstructor {
    fun <VM : CommonViewModel, VMF : BaseViewModelFactory<VM>> createViewModel(
        context: Context,
        viewModelStoreOwner: ViewModelStoreOwner,
        any: Any
    ): VM? {
        try {
            val anyClass = any::class.java
            val parameterizedType =
                anyClass.genericSuperclass as? ParameterizedType ?: return null
            val vmClazz = try {
                val clazz = parameterizedType.actualTypeArguments[1] as? Class<VM>
                if (CommonViewModel::class.java == clazz) {
                    null
                } else {
                    clazz
                }
            } catch (e: Exception) {
                null
            }
            if (vmClazz == null) {
                return null
            }
            if (any is BaseFragment<*, *, *>) {
                val activity = any.requireActivity()
                if (activity is BaseActivity<*, *, *>) {
                    val activityViewModel = activity.viewModel
                    if (activityViewModel != null) {
                        if (vmClazz.isAssignableFrom(activityViewModel::class.java)) {
                            return activityViewModel as? VM
                        }
                    }
                }

            }
            val vmfClazz = try {
                val clazz = parameterizedType.actualTypeArguments[2] as? Class<VMF>
                if (BaseViewModelFactory::class.java == clazz) {
                    null
                } else {
                    clazz
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (vmfClazz == null) {
                return ViewModelProvider(viewModelStoreOwner)[vmClazz as Class<ViewModel>] as VM
            }
            if (!BaseViewModel::class.java.isAssignableFrom(vmClazz) &&
                !BaseAndroidViewModel::class.java.isAssignableFrom(vmClazz)
            ) {
                return null
            }
            val getVMFNeedsValueMethod = try {
                anyClass.getDeclaredMethod("getVMFactoryNeedsValue")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
                null
            }
            var vmfNeedsValue: Set<Any?>? = null
            if (getVMFNeedsValueMethod != null) {
                vmfNeedsValue = (getVMFNeedsValueMethod.invoke(any) as? Set<Any?>)
            }
            val vmfInstance: VMF? = if (vmfNeedsValue.isNullOrEmpty()) {
                val vmfConstructor = vmfClazz.getDeclaredConstructor()
                vmfConstructor.newInstance()
            } else {
                val vmfNeedsValueClazz = vmfNeedsValue.map { value -> value!!::class.java }
                val vmfConstructor =
                    vmfClazz.getDeclaredConstructor(*vmfNeedsValueClazz.toTypedArray()) ?: null
                vmfConstructor?.newInstance(*vmfNeedsValue.toTypedArray())
            }
            return if (vmfInstance == null) {
                ViewModelProvider(viewModelStoreOwner)[vmClazz as Class<ViewModel>] as VM
            } else {
                ViewModelProvider(
                    viewModelStoreOwner,
                    vmfInstance
                )[vmClazz as Class<ViewModel>] as VM
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun <VDB : ViewDataBinding> createViewBinding(context: Context, any: Any): VDB? {
        try {
            val parameterizedType = this::class.java.genericSuperclass as ParameterizedType
            val viewDataBindingClazz = parameterizedType.actualTypeArguments[0] as Class<VDB>
            if (viewDataBindingClazz != ViewDataBinding::class.java)
                return inflateViewDataBindingFromClass(context, viewDataBindingClazz, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}