package xcj.app.compose_share.foundation_extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import xcj.app.starter.android.ProjectConstants
import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(showBackground = true)
annotation class DesignPreview(
    val designHook: KClass<out DesignPreviewHook> = DesignPreviewHook::class
)

interface DesignPreviewHook {
    fun onPreview()
}


class ProjectPreviewWrapperProviderImpl : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable (() -> Unit)) {
        LaunchedEffect(true) {
            ProjectConstants.IS_IN_ANDROID_STUDIO_PREVIEW = true
        }
        content()
    }

}
