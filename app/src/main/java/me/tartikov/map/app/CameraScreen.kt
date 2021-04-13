package me.tartikov.map.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.tartikov.map.*
import ru.dgis.sdk.milliseconds

private class CameraScreenViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val positions = listOf(
        CameraPosition(
            GeoPoint(55.751599, 37.620886),
            zoom = Zoom(12.0f)
        ),
        CameraPosition(
            GeoPoint(55.76201904911264, 37.577500781044364),
            zoom = Zoom(15.5f)
        ),
        CameraPosition(
            GeoPoint(55.76974051395856, 37.649533934891224),
            zoom = Zoom(17.2f)
        )
    )

    private var target by mutableStateOf<CameraPosition?>(null)

    val cameraState = CameraState(positions.first().copy(zoom = Zoom(9.0f)))

    val info: String by derivedStateOf {
        fun CameraPosition.toPrettyString(): String =
            "%.2f, %.2f x %.2f".format(point.lat, point.lon, zoom.value)

        var result = "Position: ${cameraState.position.toPrettyString()}"
        target?.let {
            result += "\nMoving to: ${it.toPrettyString()}"
        }
        result
    }

    val positionCount
        get() = positions.size

    fun onMoveButtonClicked(index: Int) {
        val position = positions[index]
        if (position == target) {
            return
        }
        coroutineScope.launch {
            target = position
            cameraState.move(position, 1500.milliseconds)
            if (target == position) {
                target = null
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { CameraScreenViewModel(coroutineScope) }
    MapView(cameraState = vm.cameraState) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Text(vm.info)
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(vm.positionCount) {
                    Button(onClick = {
                        vm.onMoveButtonClicked(it)
                    }) {
                        Text("${it + 1}")
                    }
                }
            }
        }
    }
}
