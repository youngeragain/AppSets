package xcj.app.appsets.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.theme.AppSetsTheme

class TokenErrorBottomSheetDialog(): BottomSheetDialogFragment(){
    var onDestroyListener: (() -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            AppSetsTheme {
                TokenErrorPopupPage(this)
            }
        }
        return composeView
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroyListener?.invoke()
    }
}

@Composable
fun TokenErrorPopupPage(dialogFragment: BottomSheetDialogFragment) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .padding(12.dp)) {
        Box(modifier = Modifier
            .fillMaxWidth(1f)
            .align(Alignment.TopEnd)){
            Icon(ImageVector.vectorResource(id = R.drawable.ic_round_close_24), null,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.tertiary,
                        RoundedCornerShape(12.dp)
                    )
                    .align(Alignment.CenterEnd)
                    .clickable {
                        dialogFragment.dialog?.dismiss()
                    }, tint = MaterialTheme.colorScheme.onTertiary)
        }
        Text(
            text = "登录已过期或未找到你的登录信息.\n请尝试重新登录!",
            softWrap = true,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}