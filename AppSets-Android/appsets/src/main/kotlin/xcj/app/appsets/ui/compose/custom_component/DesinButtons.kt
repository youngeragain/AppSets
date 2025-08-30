package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DesignBackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_left_24),
            contentDescription = "go back",
            modifier = Modifier
                .size(46.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun DesignDropDownButton(modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_expand_more_24),
            contentDescription = stringResource(xcj.app.appsets.R.string.dismiss),
            modifier = Modifier
                .size(46.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick)
        )
        Spacer(Modifier.height(12.dp))
    }
}