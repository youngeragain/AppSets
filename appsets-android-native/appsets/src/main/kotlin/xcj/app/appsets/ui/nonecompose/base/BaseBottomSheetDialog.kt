package xcj.app.appsets.ui.nonecompose.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xcj.app.appsets.R
import xcj.app.appsets.databinding.DialogBaseBottomsheetBinding

abstract class BaseBottomSheetDialog<VM : BaseViewModel, VBD : ViewDataBinding> :
    BottomSheetDialogFragment() {

    lateinit var baseBinding: DialogBaseBottomsheetBinding
    var binding: VBD? = null
    var viewModel: VM? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            binding = createBinding()
            if (binding != null) {
                baseBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.dialog_base_bottomsheet,
                    null, false
                )
                baseBinding.flCustomContainerRoot.addView(binding?.root)
                return baseBinding.root
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun createViewModel(): VM?
    abstract fun createBinding(): VBD?

    override fun onStart() {
        super.onStart()
        if (::baseBinding.isInitialized) {
            (baseBinding.root.parent as? FrameLayout)?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                height = if (arguments?.getBoolean("full_height") == true) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

}