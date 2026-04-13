package xcj.app.appsets.ui.compose.conversation.ai_model

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xcj.app.appsets.im.GenerativeAISessions
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer

@Composable
fun AIModelDetailPage(
    aiModelInfo: GenerativeAISessions.AIModelInfo?,
    onBackClick: () -> Unit,
) {
    if (aiModelInfo == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.not_found),
                modifier = Modifier.align(
                    Alignment.Center
                )
            )

            DesignBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部 Header
                StatusBarWithTopActionBarSpacer()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnyImage(
                        Modifier
                            .size(250.dp)
                            .clip(ExtraLarge2)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = ExtraLarge2
                            ),
                        model = aiModelInfo.bioUrl
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = aiModelInfo.bioName ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = aiModelInfo.description ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (aiModelInfo.models.isNotEmpty()) {
                        Text(
                            text = "Available Models",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        // 展示具体模型列表
                        aiModelInfo.models.forEach { model ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = model.name,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            DesignBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }
    }
}