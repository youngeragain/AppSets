package xcj.app.appsets.ui.compose.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.theme.AppSetsShapes

@Composable
fun UserApplications(
    userApplications: List<Application>?,
    onBioClick: (Bio) -> Unit
) {
    if (userApplications.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.no_application), fontSize = 12.sp)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(90.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(userApplications) { application ->
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnyImage(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(AppSetsShapes.large)
                            .background(MaterialTheme.colorScheme.outline, AppSetsShapes.large)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                AppSetsShapes.large
                            )
                            .clickable {
                                onBioClick.invoke(application)
                            },
                        any = application.bioUrl
                    )
                    Text(
                        text = application.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .widthIn(max = 82.dp)
                    )
                }
            }
        }
    }
}