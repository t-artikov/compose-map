package me.tartikov.map

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.map.CircleOptions
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.PolygonOptions
import ru.dgis.sdk.map.PolylineOptions

internal typealias MapObjectId = Long

internal class MapObjectManager(private val dgisManager: DGisMapObjectManager) :
    ObjectTouchEventProcessor, AutoCloseable {
    private val objects = mutableMapOf<MapObjectId, MapObjectNode>()

    fun addCircle(
        id: MapObjectId,
        position: GeoPoint,
        radius: Meter,
        color: Color,
        strokeWidth: Dp,
        strokeColor: Color,
        onClick: (() -> Unit)?
    ) {
        (objects[id] as? CircleNode)?.let {
            if (it.position != position) {
                it.position = position
                it.dgisObject.position = position
            }
            if (it.radius != radius) {
                it.radius = radius
                it.dgisObject.radius = radius
            }
            if (it.color != color) {
                it.color = color
                it.dgisObject.color = color.toDGis()
            }
            if (it.strokeWidth != strokeWidth) {
                it.strokeWidth = strokeWidth
                it.dgisObject.strokeWidth = strokeWidth.toDGis()
            }
            if (it.strokeColor != strokeColor) {
                it.strokeColor = strokeColor
                it.dgisObject.strokeColor = strokeColor.toDGis()
            }
            applyCommonOptions(it, onClick)
            return
        }

        val dgisObject = dgisManager.addCircle(
            CircleOptions(
                position,
                radius,
                color.toDGis(),
                strokeWidth.toDGis(),
                strokeColor.toDGis()
            )
        )
        objects[id] =
            CircleNode(dgisObject, position, radius, color, strokeWidth, strokeColor).also {
                applyCommonOptions(it, onClick)
            }
    }

    fun addPolyline(
        id: MapObjectId,
        points: List<GeoPoint>,
        width: Dp,
        color: Color,
        onClick: (() -> Unit)?
    ) {
        (objects[id] as? PolylineNode)?.let {
            if (it.points != points) {
                it.points = points
                it.dgisObject.points = points
            }
            if (it.width != width) {
                it.width = width
                it.dgisObject.width = width.toDGis()
            }
            if (it.color != color) {
                it.color = color
                it.dgisObject.color = color.toDGis()
            }
            applyCommonOptions(it, onClick)
            return
        }

        val dgisObject = dgisManager.addPolyline(
            PolylineOptions(
                points,
                width.toDGis(),
                color.toDGis()
            )
        )
        objects[id] = PolylineNode(dgisObject, points, width, color).also {
            applyCommonOptions(it, onClick)
        }
    }

    fun addPolygon(
        id: MapObjectId,
        contour: List<GeoPoint>,
        holes: List<List<GeoPoint>>?,
        color: Color,
        strokeWidth: Dp,
        strokeColor: Color,
        onClick: (() -> Unit)?
    ) {
        fun getContours(contour: List<GeoPoint>, holes: List<List<GeoPoint>>?): List<List<DGisGeoPoint>> {
            val result = mutableListOf(contour)
            if (holes != null) {
                result.addAll(holes)
            }
            return result
        }

        (objects[id] as? PolygonNode)?.let {
            if (it.contour != contour || it.holes != it.holes) {
                it.contour = contour
                it.holes = holes
                it.dgisObject.contours = getContours(contour, holes)
            }
            if (it.color != color) {
                it.color = color
                it.dgisObject.color = color.toDGis()
            }
            if (it.strokeWidth != strokeWidth) {
                it.strokeWidth = strokeWidth
                it.dgisObject.strokeWidth = strokeWidth.toDGis()
            }
            if (it.strokeColor != strokeColor) {
                it.strokeColor = strokeColor
                it.dgisObject.strokeColor = strokeColor.toDGis()
            }
            applyCommonOptions(it, onClick)
            return
        }

        val dgisObject = dgisManager.addPolygon(
            PolygonOptions(
                getContours(contour, holes),
                color.toDGis(),
                strokeWidth.toDGis(),
                strokeColor.toDGis()
            )
        )
        objects[id] = PolygonNode(dgisObject, contour, holes, color, strokeWidth, strokeColor).also {
            applyCommonOptions(it, onClick)
        }
    }

    fun addMarker(
        id: MapObjectId,
        position: GeoPoint,
        icon: Image?,
        anchor: Alignment,
        text: String,
        textStyle: TextStyle?,
        onClick: (() -> Unit)?,
        onDrag: ((DragEvent) -> Unit)?
    ) {
        (objects[id] as? MarkerNode)?.let {
            if (it.position != position) {
                it.position = position
                it.dgisObject.position = position.toDGisWithElevation()
            }
            if (it.icon != icon) {
                it.icon = icon
                it.dgisObject.icon = icon
            }
            if (it.anchor != anchor) {
                it.anchor = anchor
                it.dgisObject.anchor = anchor.toDGis()
            }
            if (it.text != text) {
                it.text = text
                it.dgisObject.text = text
            }
            if (it.textStyle != textStyle) {
                it.textStyle = textStyle
                it.dgisObject.textStyle = textStyle?.toDGis() ?: DGisTextStyle()
            }
            if (it.onDrag != onDrag) {
                it.onDrag = onDrag
                it.dgisObject.isDraggable = onDrag != null
            }
            applyCommonOptions(it, onClick, onDrag != null)
            return
        }

        val dgisObject = dgisManager.addMarker(
            MarkerOptions(
                position.toDGisWithElevation(),
                icon = icon,
                anchor = anchor.toDGis(),
                text = text,
                textStyle = textStyle?.toDGis(),
                draggable = onDrag != null
            )
        )
        objects[id] = MarkerNode(dgisObject, position, icon, anchor, text, textStyle, onDrag).also {
            applyCommonOptions(it, onClick, onDrag != null)
        }
    }

    fun removeObject(id: MapObjectId) {
        objects[id]?.dgisObject?.let {
            objects.remove(id)
            it.remove()
            it.close()
        }
    }

    private fun applyCommonOptions(node: MapObjectNode, onClick: (() -> Unit)?, forceUserData: Boolean = false) {
        if (node.onClick != onClick) {
            node.onClick = onClick
        }

        val userData = if (onClick != null || forceUserData) node else null
        if (node.userData != userData) {
            node.userData = userData
            node.dgisObject.userData = userData
        }
    }

    override fun hasClickable(): Boolean {
        return objects.values.any {
            it.onClick != null
        }
    }

    override fun onClick(userData: Any?): Boolean {
        val onClick = (userData as? MapObjectNode)?.onClick ?: return false
        onClick()
        return true
    }

    override fun onDrag(event: DragEvent, userData: Any?) {
        val onDrag = (userData as? MarkerNode)?.onDrag ?: return
        onDrag(event)
    }

    override fun close() {
        dgisManager.close()
        objects.values.forEach {
            it.dgisObject.close()
        }
        objects.clear()
    }
}

internal val LocalMapObjectManager =
    compositionLocalOf<MapObjectManager>(referentialEqualityPolicy()) {
        error("Can't create map objects outside of MapView")
    }

private abstract class MapObjectNode {
    abstract val dgisObject: DGisSimpleMapObject
    var onClick: (() -> Unit)? = null
    var userData: Any? = null
}

private class CircleNode(
    override val dgisObject: DGisCircle,
    var position: GeoPoint,
    var radius: Meter,
    var color: Color,
    var strokeWidth: Dp,
    var strokeColor: Color
) : MapObjectNode()

private class PolylineNode(
    override val dgisObject: DGisPolyline,
    var points: List<GeoPoint>,
    var width: Dp,
    var color: Color
) : MapObjectNode()

private class PolygonNode(
    override val dgisObject: DGisPolygon,
    var contour: List<GeoPoint>,
    var holes: List<List<GeoPoint>>?,
    var color: Color,
    var strokeWidth: Dp,
    var strokeColor: Color
) : MapObjectNode()

private class MarkerNode(
    override val dgisObject: DGisMarker,
    var position: GeoPoint,
    var icon: Image?,
    var anchor: Alignment = Alignment.Center,
    var text: String,
    var textStyle: TextStyle?,
    var onDrag: ((DragEvent) -> Unit)?
) : MapObjectNode()

private var currentId: MapObjectId = 0

@Composable
private fun rememberMapObjectId(): MapObjectId {
    return remember {
        currentId++
        currentId
    }
}

@Composable
private inline fun MapObject(
    crossinline add: MapObjectManager.(MapObjectId) -> Unit
) {
    val id = rememberMapObjectId()
    val manager = LocalMapObjectManager.current
    SideEffect {
        manager.add(id)
    }
    DisposableEffect(Unit) {
        onDispose {
            manager.removeObject(id)
        }
    }
}

@Composable
fun Circle(
    position: GeoPoint,
    radius: Meter,
    color: Color,
    strokeWidth: Dp = Dp(0.0f),
    strokeColor: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    MapObject {
        addCircle(it, position, radius, color, strokeWidth, strokeColor, onClick)
    }
}

@Composable
fun Polyline(
    points: List<GeoPoint>,
    width: Dp = 1.dp,
    color: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    MapObject {
        addPolyline(it, points, width, color, onClick)
    }
}

@Composable
fun Point(
    position: GeoPoint,
    size: Dp = 1.dp,
    color: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    Polyline(
        points = remember(position) { listOf(position, position) },
        width = size,
        color = color,
        onClick
    )
}

@Composable
fun Polygon(
    contour: List<GeoPoint>,
    holes: List<List<GeoPoint>>? = null,
    color: Color = Color.Black,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    MapObject {
        addPolygon(it, contour, holes, color, strokeWidth, strokeColor, onClick)
    }
}

@Composable
fun Rectangle(
    contour: GeoRect,
    color: Color = Color.Black,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    Polygon(
        contour = remember(contour) {
            listOf(
                GeoPoint(contour.minLat, contour.minLon),
                GeoPoint(contour.minLat, contour.maxLon),
                GeoPoint(contour.maxLat, contour.maxLon),
                GeoPoint(contour.maxLat, contour.minLon)
            )
        },
        color = color,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        onClick = onClick
    )
}

@Composable
fun Marker(
    position: GeoPoint,
    icon: Image?,
    anchor: Alignment = Alignment.Center,
    text: String = "",
    textStyle: TextStyle? = null,
    onClick: (() -> Unit)? = null,
    onDrag: ((DragEvent) -> Unit)? = null
) {
    MapObject {
        addMarker(it, position, icon, anchor, text, textStyle, onClick, onDrag)
    }
}

@Composable
fun Label(
    position: GeoPoint,
    text: String,
    style: TextStyle? = null,
    onClick: (() -> Unit)? = null,
    onDrag: ((DragEvent) -> Unit)? = null
) {
    Marker(position, null, text = text, textStyle = style, onClick = onClick, onDrag = onDrag)
}
