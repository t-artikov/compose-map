package me.tartikov.map

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import ru.dgis.sdk.coordinates.Arcdegree

fun GeoPoint.toDGisWithElevation() = DGisGeoPointWithElevation(Arcdegree(lat), Arcdegree((lon)))

fun Color.toDGis() = DGisColor(toArgb())

private fun dpToZpt(value: Float): Float {
    return value * 96.0f / 160.0f
}

fun Dp.toDGis() = DGisLogicalPixel(dpToZpt(value))

fun TextStyle.toDGis() = DGisTextStyle(
    fontSize = if (fontSize.isSp) dpToZpt(fontSize.value) else 8.0f,
    color = color.toDGis(),
    strokeColor = shadow?.color?.toDGis() ?: color.toDGis(),
    strokeWidth = if (shadow != null) dpToZpt(shadow!!.blurRadius) else 0.0f
)

fun Alignment.toDGis(): DGisAnchor {
    val scale = 1000
    align(IntSize.Zero, IntSize(scale, scale), LayoutDirection.Ltr).let {
        return DGisAnchor(it.x.toFloat() / scale, it.y.toFloat() / scale)
    }
}
