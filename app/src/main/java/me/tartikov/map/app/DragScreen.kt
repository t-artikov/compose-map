package me.tartikov.map.app

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tartikov.map.*

private val bounds = GeoRect(
    GeoPoint(55.56093715954094, 37.32648122124374),
    GeoPoint(55.92053080950464, 37.89095948450267)
)

private class Pin(position: GeoPoint) {
    var position by mutableStateOf(position)
        private set
    var dragged by mutableStateOf(false)
        private set

    fun onDrag(event: DragEvent) {
        dragged = event.action != DragAction.End
        position = bounds.clamp(event.point)
    }
}

private class DragScreenViewModel {
    val pins = listOf(
        Pin(GeoPoint(55.766771325139466, 37.40078377537429)),
        Pin(GeoPoint(55.86305926451473, 37.51464723609388)),
        Pin(GeoPoint(55.85868345216242, 37.736962381750345)),
        Pin(GeoPoint(55.75935832894735, 37.796886367723346)),
        Pin(GeoPoint(55.62222191809323, 37.69321429543197)),
        Pin(GeoPoint(55.64420550251269, 37.478680992498994))
    )
}

@Composable
fun DragScreen() {
    val vm = remember { DragScreenViewModel() }
    val pinImage = imageFromResource(R.drawable.ic_pin)
    val markerImage = imageFromResource(R.drawable.ic_marker)

    MapView {
        vm.pins.forEach {
            Marker(
                position = it.position,
                icon = if (it.dragged) markerImage else pinImage,
                anchor = if (it.dragged) Alignment.BottomCenter else Alignment.Center,
                onDrag = it::onDrag
            )
        }
        Polygon(
            contour = vm.pins.map { it.position },
            color = Color.Blue.copy(alpha = 0.2f),
            strokeWidth = 2.dp,
            strokeColor = Color.Blue
        )
        Rectangle(
            contour = bounds,
            color = Color.Transparent,
            strokeWidth = 1.dp,
            strokeColor = Color.Red
        )
    }
}
