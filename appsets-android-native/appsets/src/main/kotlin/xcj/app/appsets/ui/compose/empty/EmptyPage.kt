package xcj.app.appsets.ui.compose.empty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Preview(showBackground = true)
@Composable
fun EmptyPagePreview() {
    EmptyPage()
}


@Composable
fun EmptyPage(navController: NavController? = null) {
    Box {
        Column {
            Text(text = "贤者时间", modifier = Modifier.align(Alignment.CenterHorizontally))
            var isCheckToday by remember {
                mutableStateOf(false)
            }
            if (isCheckToday) {
                Text(text = "今日已打卡")
            } else {
                Text(text = "打卡", modifier = Modifier.clickable {
                    isCheckToday = true
                })
            }
        }
    }
}