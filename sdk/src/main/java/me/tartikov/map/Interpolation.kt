package me.tartikov.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private inline fun <T> interpolate(values: List<Pair<Float, T>>, t: Float, lerp: (T, T, Float) -> T): T {
    if (t < values.first().first) {
        return values.first().second
    }
    values.zipWithNext { a, b ->
        if (t >= a.first && t <= b.first) {
            val k = (t - a.first) / (b.first - a.first)
            return lerp(a.second, b.second, k)
        }
    }
    return values.last().second
}

@Composable
fun zoomDependentColor(colors: List<Pair<Float, Color>>): Color {
    return interpolate(colors, Camera.zoom, ::lerp)
}

@Composable
fun zoomDependentColor(vararg colors: Pair<Float, Color>): Color {
    return zoomDependentColor(colors.asList())
}
