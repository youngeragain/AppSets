@file:OptIn(InternalResourceApi::class)

package appsets_android.app_multiplatform.generated.resources

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceContentHash
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String =
    "composeResources/appsets_android.app_multiplatform.generated.resources/"

@delegate:ResourceContentHash(1_942_773_276)
internal val Res.drawable.compose_multiplatform: DrawableResource by lazy {
    DrawableResource(
        "drawable:compose_multiplatform", setOf(
            ResourceItem(setOf(), "${MD}drawable/compose-multiplatform.xml", -1, -1),
        )
    )
}

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
    map.put("compose_multiplatform", Res.drawable.compose_multiplatform)
}
