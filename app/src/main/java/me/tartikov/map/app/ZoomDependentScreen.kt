package me.tartikov.map.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tartikov.map.*

@Composable
fun ZoomDependentScreen() {
    MapView {
        val color = zoomDependentColor(
            8.0f to Color.Green,
            10.0f to Color.Red,
            15.0f to Color.Blue
        )
        val zoom = Camera.zoom
        val position = GeoPoint(55.750582815654376, 37.62009685859084)
        val text = if (zoom > 8.0f && zoom < 15.0f) "Zoom it" else "Enough"
        Point(position, size = 100.dp, color = color)
        Label(position, text, style = TextStyle(Color.White, fontSize = 20.sp))
    }
}
