package me.tartikov.map

import ru.dgis.sdk.map.DragBeginData
import ru.dgis.sdk.map.RenderedObjectInfo
import ru.dgis.sdk.map.ScreenDistance

data class TouchEvent(val point: GeoPoint)

enum class DragAction {
    Begin,
    Move,
    End
}
data class DragEvent(val action: DragAction, val point: GeoPoint)

internal interface ObjectTouchEventProcessor {
    fun hasClickable(): Boolean
    fun onClick(userData: Any?): Boolean
    fun onDrag(event: DragEvent, userData: Any?)
}

// make GC happy
private fun List<RenderedObjectInfo>.closeAll() {
    forEach {
        it.item.item.close()
        it.item.source.close()
    }
}

private fun DragBeginData.close() {
    item.item.close()
    item.source.close()
}

internal class TouchEventProcessor(
    private val map: DGisMap,
    private val onClick: (TouchEvent) -> Unit,
    private val projection: DGisProjection,
    private val objectTouchEventProcessor: ObjectTouchEventProcessor
) : DGisTouchEventsObserver, AutoCloseable {

    private class Drag(val userData: Any, var point: GeoPoint)
    private var drag: Drag? = null

    override fun onTap(point: DGisScreenPoint) {
        if (!objectTouchEventProcessor.hasClickable()) {
            val event = createTouchEvent(point) ?: return
            onClick(event)
            return
        }
        map.getRenderedObjects(point, ScreenDistance(5f)).onResult { objects ->
            val userData = objects.firstOrNull()?.item?.item?.userData
            objects.closeAll()
            if (objectTouchEventProcessor.onClick(userData)) {
                return@onResult
            }
            val event = createTouchEvent(point) ?: return@onResult
            onClick(event)
        }
    }

    override fun onDragBegin(data: DragBeginData) {
        val userData = data.item.item.userData
        data.close()
        userData ?: return
        val point = projection.screenToMap(data.point) ?: return
        drag = Drag(userData, point)
        onDrag(DragEvent(DragAction.Begin, point), userData)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onDragMove(screenPoint: DGisScreenPoint) {
        val drag = this.drag ?: return
        val point = projection.screenToMap(screenPoint) ?: return
        drag.point = point
        onDrag(DragEvent(DragAction.Move, point), drag.userData)
    }

    override fun onDragEnd() {
        val drag = this.drag ?: return
        onDrag(DragEvent(DragAction.End, drag.point), drag.userData)
    }

    private fun createTouchEvent(screenPoint: DGisScreenPoint): TouchEvent? {
        val point = projection.screenToMap(screenPoint) ?: return null
        return TouchEvent(point)
    }

    private fun onDrag(event: DragEvent, userData: Any?) {
        objectTouchEventProcessor.onDrag(event, userData)
    }

    override fun close() {
        projection.close()
    }
}
