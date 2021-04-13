package me.tartikov.map.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tartikov.map.*

private class PinWithOverlay(val position: GeoPoint) {
    var overlayVisible by mutableStateOf(true)
        private set

    fun onClick() {
        overlayVisible = !overlayVisible
    }
}

private class OverlayScreenViewModel {
    val pins = listOf(
        PinWithOverlay(GeoPoint(55.82956359813082, 37.43315267376602)),
        PinWithOverlay(GeoPoint(55.87069802076889, 37.63721496798098)),
        PinWithOverlay(GeoPoint(55.773569368507836, 37.81083242967725)),
        PinWithOverlay(GeoPoint(55.70033164936125, 37.3920111078769)),
        PinWithOverlay(GeoPoint(55.70265142170993, 37.625695299357176)),
        PinWithOverlay(GeoPoint(55.577652949068835, 37.76722230948508))
    )
}

@Composable
fun OverlayScreen() {
    val vm = remember { OverlayScreenViewModel() }
    val pinImage = imageFromResource(R.drawable.ic_pin)
    MapView {
        vm.pins.forEach {
            PinWithOverlayView(it, pinImage)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PinWithOverlayView(pin: PinWithOverlay, icon: Image) {
    val zoom = Camera.zoom
    AnimatedVisibility(
        pin.overlayVisible && zoom > 8.5f,
        Modifier
            .snapToGeoPoint(pin.position, Alignment.BottomCenter)
            .padding(12.dp)
    ) {
        Button(onClick = pin::onClick) {
            Text("Click Me")
        }
    }
    Marker(pin.position, icon, onClick = pin::onClick)
}
