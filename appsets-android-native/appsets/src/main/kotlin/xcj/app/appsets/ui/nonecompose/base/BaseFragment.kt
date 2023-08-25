package xcj.app.appsets.ui.nonecompose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment


abstract class BaseFragment<VDB : ViewDataBinding, VM : CommonViewModel, VMF : BaseViewModelFactory<VM>> :
    Fragment() {
    var viewModel: VM? = null
    var binding: VDB? = null
    var baseBinding: ViewDataBinding? = null
    open fun createBy(): String {
        return "default"
    }

    open fun createBinding(): VDB? {
        return null
    }

    open fun createViewModel(): VM? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (createBy()) {
            "default" -> {
                viewModel = createViewModel()
            }

            "reflection" -> {
                val fullReflectionConstructor = FullReflectionConstructor()
                viewModel = fullReflectionConstructor.createViewModel(requireContext(), this, this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        when (createBy()) {
            "default" -> {
                binding = createBinding()
                if (baseBinding != null) {
                    if (binding != null)
                        (baseBinding!!.root as ViewGroup).addView(binding!!.root)
                }
                if (baseBinding != null)
                    return baseBinding!!.root
                else if (binding != null)
                    return binding!!.root
                viewModel = createViewModel()
            }

            "reflection" -> {
                val fullReflectionConstructor = FullReflectionConstructor()
                binding = fullReflectionConstructor.createViewBinding(requireContext(), this)
                if (baseBinding != null) {
                    if (binding != null)
                        (baseBinding!!.root as ViewGroup).addView(binding!!.root)
                }
                if (baseBinding != null)
                    return baseBinding!!.root
                else if (binding != null)
                    return binding!!.root
                viewModel = fullReflectionConstructor.createViewModel(requireContext(), this, this)
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}