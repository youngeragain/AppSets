package xcj.app.appsets.ui.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExtraLarge2 = RoundedCornerShape(68.dp)

val ExtShapes =
    Shapes(
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )

val Shapes = Shapes()

val Shapes.extShapes: Shapes
    get() = ExtShapes

