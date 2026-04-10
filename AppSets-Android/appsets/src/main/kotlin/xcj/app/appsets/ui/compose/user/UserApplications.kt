package xcj.app.appsets.ui.compose.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.apps.SimpleApplicationList

@Composable
fun UserApplications(
    userApplications: List<Application>,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    if (userApplications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.no_application), fontSize = 12.sp)
        }
    } else {
        SimpleApplicationList(
            apps = userApplications,
            onBioClick = onBioClick,
            onApplicationLongPress = onApplicationLongPress
        )
    }
}