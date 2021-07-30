package me.tartikov.map

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.map.LogicalPixel

internal fun GeoPoint.toDGisWithElevation() = DGisGeoPointWithElevation(Arcdegree(lat), Arcdegree((lon)))

internal fun Color.toDGis() = DGisColor(toArgb())

internal fun Dp.toDGis() = DGisLogicalPixel(value)

internal fun TextStyle.toDGis() = DGisTextStyle(
    fontSize = if (fontSize.isSp) DGisLogicalPixel(fontSize.value) else DGisLogicalPixel(8.0f),
    color = color.toDGis(),
    strokeColor = shadow?.color?.toDGis() ?: color.toDGis(),
    strokeWidth = if (shadow != null) DGisLogicalPixel(shadow!!.blurRadius) else DGisLogicalPixel(0.0f)
)

internal fun Alignment.toDGis(): DGisAnchor {
    val scale = 1000
    align(IntSize.Zero, IntSize(scale, scale), LayoutDirection.Ltr).let {
        return DGisAnchor(it.x.toFloat() / scale, it.y.toFloat() / scale)
    }
}
