package me.tartikov.map

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import ru.dgis.sdk.Duration

class CameraState(initialPosition: CameraPosition) {
    private val _position = mutableStateOf(initialPosition)
    private val node = MutableStateFlow<CameraNode?>(null)

    var position: CameraPosition
        get() = _position.value
        set(value) {
            _position.value = value
            node.value?.setPositionFromState(value)
        }

    suspend fun move(position: CameraPosition, duration: Duration) {
        val node = withTimeoutOrNull(duration.inMilliseconds) {
            node.first { it != null }
        }
        if (node != null) {
            node.moveFromState(position, duration)
        } else {
            this.position = position
        }
    }

    internal fun attachToNode(node: CameraNode) {
        node.setPositionFromState(position)
        this.node.value = node
    }

    internal fun detachFromNode() {
        this.node.value = null
    }
}

@Composable
fun rememberCameraState(position: CameraPosition): CameraState {
    return remember { CameraState(position) }
}

internal class CameraNode(
    private val dgisCamera: DGisCamera,
    private val state: CameraState
) : AutoCloseable {
    private val projection = dgisCamera.projection
    private val closeables = mutableListOf<AutoCloseable>(dgisCamera, projection)
    private val visibleRectChannel = dgisCamera.visibleRectChannel
    private var settingPositionToState = false

    var point by mutableStateOf(dgisCamera.position.point)
        private set

    var zoom by mutableStateOf(dgisCamera.position.zoom.value)
        private set

    var visibleRect by mutableStateOf(dgisCamera.visibleRect)
        private set

    init {
        closeables.add(dgisCamera.positionChannel.connect(DirectExecutor) {
            zoom = it.zoom.value
            point = it.point
            settingPositionToState = true
            state.position = it
            settingPositionToState = false
        })
        closeables.add(visibleRectChannel.connect(DirectExecutor) {
            visibleRect = it
        })
        state.attachToNode(this)
    }

    @Composable
    fun toScreen(point: GeoPoint): Offset {
        visibleRect
        val screenPoint =
            projection.mapToScreen(point) ?: return Offset(-100000.0f, -100000.0f)
        return Offset(screenPoint.x, screenPoint.y)
    }

    fun setPositionFromState(position: CameraPosition) {
        if (settingPositionToState) {
            return
        }
        dgisCamera.position = position
    }

    suspend fun moveFromState(position: CameraPosition, duration: Duration) {
        dgisCamera.move(position, duration).await()
    }

    override fun close() {
        closeables.forEach(AutoCloseable::close)
        state.detachFromNode()
    }
}

internal val LocalCamera = staticCompositionLocalOf<CameraNode> {
    error("Can't access to Camera outside of MapView")
}

object Camera {
    val point: GeoPoint
        @ReadOnlyComposable
        @Composable
        get() = LocalCamera.current.point

    val zoom: Float
        @ReadOnlyComposable
        @Composable
        get() = LocalCamera.current.zoom

    val visibleRect: GeoRect
        @ReadOnlyComposable
        @Composable
        get() = LocalCamera.current.visibleRect

    @Composable
    fun toScreen(point: GeoPoint): Offset {
        return LocalCamera.current.toScreen(point)
    }
}
