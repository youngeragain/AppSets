package xcj.app.appsets.ui.nonecompose.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.nonecompose.base.BaseBottomSheetDialog
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel

class UserAgreementBottomSheetDialog : BaseBottomSheetDialog<BaseViewModel, ViewDataBinding>() {
    var onClick: ((DialogFragment, Boolean) -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            AppSetsTheme {
                UserAgreementPopupPage {
                    onClick?.invoke(this, it)
                }
            }
        }
        return composeView
    }

    override fun createViewModel(): BaseViewModel? {
        return null
    }

    override fun createBinding(): ViewDataBinding? {
        return null
    }
}

@Composable
fun UserAgreementPopupPage(onClick: (Boolean) -> Unit) {
    val context = LocalContext.current
    val agreementText = context.getString(R.string.user_agreement)
    Column(Modifier.padding(12.dp)) {
        Text(text = "AppSets", fontSize = 32.sp, softWrap = true)
        Text(text = "用户协议&数据政策", fontSize = 32.sp, softWrap = true)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = agreementText, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            onClick(true)
        }) {
            Text(text = "同意AppSets的政策")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            onClick(false)
        }) {
            Text(text = "不同意AppSets的政策")
        }
    }
}