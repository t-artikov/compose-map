package me.tartikov.map.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tartikov.map.*
import kotlin.random.Random

private fun GeoRect.randomPoint() = GeoPoint(
    Random.nextDouble(minLat, maxLat),
    Random.nextDouble(minLon, maxLon)
)

private class AnimationScreenViewModel {
    private val bounds = GeoRect(
        GeoPoint(55.56093715954094, 37.32648122124374),
        GeoPoint(55.92053080950464, 37.89095948450267)
    )
    val points = List(50) { bounds.randomPoint() }
}

@Composable
fun AnimationScreen() {
    val vm = remember { AnimationScreenViewModel() }

    MapView {
        vm.points.forEach {
            val active = distance(it, Camera.point).value < 4000
            AnimatedPoint(it, active)
        }
    }
}

@Composable
fun AnimatedPoint(position: GeoPoint, active: Boolean) {
    val size by animateDpAsState(if (active) 20.dp else 10.dp,
        animationSpec = spring(dampingRatio = 0.3f)
    )
    val color by animateColorAsState(if (active) Color.Red else Color.Blue,
        animationSpec = tween(500)
    )
    Point(position, size, color)
}
