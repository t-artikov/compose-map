package me.tartikov.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

private class MapNode(
    val camera: CameraNode,
    val objectManager: MapObjectManager,
    private val closeables: List<AutoCloseable>
) : AutoCloseable {
    override fun close() {
        closeables.forEach(AutoCloseable::close)
    }
}

private class MapOptions(
    val onClick: (TouchEvent) -> Unit,
    val cameraState: CameraState
)

private val DefaultCameraPosition = CameraPosition(
    GeoPoint(55.740444, 37.619524),
    Zoom(9.0f)
)

// heh, fix it in SDK
private fun DGisMapView.destroy() {
    javaClass.getDeclaredMethod("destroy").apply {
        isAccessible = true
        invoke(this@destroy)
    }
}

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onClick: (TouchEvent) -> Unit = {},
    cameraState: CameraState = remember { CameraState(DefaultCameraPosition) },
    content: (@Composable () -> Unit)? = null
) {
    val mapView = remember { mutableStateOf<DGisMapView?>(null) }
    val mapNode = remember { mutableStateOf<MapNode?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    Box(modifier = modifier) {
        AndroidView(
            factory = {
                val options = MapOptions(onClick, cameraState)
                createDGisMapView(it, options) { map, view ->
                    mapNode.value = createMapNode(map, view, options)
                }.apply {
                    mapView.value = this
                    lifecycle.addObserver(this)
                }
            }
        )
        mapNode.value?.let {
            if (content != null) {
                MapProvider(it, content)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mapNode.value?.close()
            mapNode.value = null
            mapView.value?.destroy()
            mapView.value = null
        }
    }
}

private fun createDGisMapView(
    context: Context,
    options: MapOptions,
    onMapReady: (DGisMap, DGisMapView) -> Unit
): DGisMapView {
    return DGisMapView(context, DGisMapOptions().apply {
        position = options.cameraState.position
    }).apply {
        getMapAsync {
            onMapReady(it, this)
        }
    }
}

private fun createMapNode(map: DGisMap, view: DGisMapView, options: MapOptions): MapNode {
    val dgisCamera = map.camera
    val camera = CameraNode(dgisCamera, options.cameraState)
    val objectManager = MapObjectManager(DGisMapObjectManager(map))

    val touchEventProcessor =
        TouchEventProcessor(map, options.onClick, dgisCamera.projection, objectManager)
    view.setTouchEventsObserver(touchEventProcessor)
    val closeables = listOf(map, objectManager, camera, touchEventProcessor)

    return MapNode(camera, objectManager, closeables)
}

@Composable
private fun MapProvider(mapNode: MapNode, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalCamera provides mapNode.camera,
        LocalMapObjectManager provides mapNode.objectManager,
    ) {
        content()
    }
}
