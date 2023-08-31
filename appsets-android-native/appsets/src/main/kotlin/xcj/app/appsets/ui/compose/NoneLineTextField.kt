package xcj.app.appsets.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoneLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.large,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
    val animatedThickness = rememberUpdatedState(2.dp)
    val stoke = rememberUpdatedState(
        BorderStroke(animatedThickness.value, SolidColor(indicatorColor))
    )
    BasicTextField(
        value = value,
        modifier = modifier
            .background(backgroundColor, shape)
            /* .drawIndicatorLineWin11Style(stoke.value)*/
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight
            ),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                container = @Composable {
                    Box(
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                    )
                }
            )
        }
    )
}


internal fun Modifier.drawIndicatorLineWin11Style(
    indicatorBorder: BorderStroke,
): Modifier {
    val strokeWidthDp = indicatorBorder.width

    return drawWithContent {
        drawContent()
        if (strokeWidthDp == Dp.Hairline) return@drawWithContent
        val strokeWidth = strokeWidthDp.value * density
        val lineY = size.height - strokeWidth/2
        /*val toPx1 = roundedCornerShape.bottomStart.toPx(size, localDensity)
        val toPx2 = roundedCornerShape.bottomEnd.toPx(size, localDensity)
        val arc1Y = size.height - toPx1*2
        val arc2Y = size.height - toPx2*2
        val arc1X = toPx1
        val arc2X = size.width-toPx2*2
        Log.e("LineWin11Style", """
            arc1X:$arc1X
            arc1Y:$arc1Y
            arc2X:$arc2X
            arc2Y:$arc2Y
            width:${size.width}
            height:${size.height}
        """.trimIndent())*/
        val arcRadius = 8.dp.toPx()
        val arcY = size.height-arcRadius*2
        val arc2X = size.width-arcRadius*2
        //drawArc(indicatorBorder.brush, 90f, 90f, true, topLeft = Offset(0f, arcY), size = Size(arcRadius, arcRadius))
        drawLine(
            indicatorBorder.brush,
            Offset(arcRadius, lineY),
            Offset(arc2X, lineY),
            strokeWidth
        )
        //drawArc(indicatorBorder.brush, 0f, 90f, true, topLeft = Offset(arc2X, arcY), size = Size(arcRadius, arcRadius))
    }
}
