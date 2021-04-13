package me.tartikov.map.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.red
import androidx.core.math.MathUtils.clamp
import com.yeo.javasupercluster.SuperCluster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import me.tartikov.map.*
import kotlin.random.Random

typealias GeoJsonPoint = org.wololo.geojson.Point
typealias GeoJsonFeature = org.wololo.geojson.Feature

private sealed class ObjectInfo(val position: GeoPoint)
private class MarkerInfo(position: GeoPoint, val isCrown: Boolean) : ObjectInfo(position)
private class ClusterInfo(position: GeoPoint, val count: Int) : ObjectInfo(position)

private fun toObjectInfo(feature: GeoJsonFeature): ObjectInfo {
    val geometry = feature.geometry as GeoJsonPoint
    val position = GeoPoint(geometry.coordinates[1], geometry.coordinates[0])
    val count = feature.properties?.get("point_count") as? Int
    return if (count == null) {
        val isCrown = (feature.properties?.get("crown") as? Boolean) ?: false
        MarkerInfo(position, isCrown)
    } else {
        ClusterInfo(position, count)
    }
}

private fun createClustering(mask: Bitmap): SuperCluster {
    val random = Random(1)

    val bounds = GeoRect(
        GeoPoint(-60.0, -180.0),
        GeoPoint(85.0, 180.0)
    )

    fun isEarth(lat: Double, lon: Double): Boolean {
        val rx = (lon + 180.0) / 360.0
        val ry = (lat + 90.0) / 180.0
        val x = clamp((rx * mask.width).toInt(), 0, mask.width - 1)
        val y = clamp(((1.0 - ry) * mask.height).toInt(), 0, mask.height - 1)
        return mask.getPixel(x, y).red < 100
    }

    fun randomEarthPoint(): GeoJsonPoint {
        while (true) {
            val lat = Random.nextDouble(bounds.minLat, bounds.maxLat)
            val lon = random.nextDouble(bounds.minLon, bounds.maxLon)
            if (isEarth(lat, lon)) {
                return GeoJsonPoint(doubleArrayOf(lon, lat))
            }
        }
    }

    return SuperCluster(25, 256, 0, 7, 64, Array(100000) {
        val isCrown = Random.nextDouble() < 0.1
        val properties = if (isCrown) mapOf("crown" to true) else null
        GeoJsonFeature(randomEarthPoint(), properties)
    })
}

private fun SuperCluster.getObjects(rect: GeoRect, zoom: Int): List<ObjectInfo> {
    val bounds = with(rect) {
        doubleArrayOf(minLon, minLat, maxLon, maxLat)
    }
    return getClusters(bounds, zoom).map(::toObjectInfo)
}

private class EarthMaskLoader(private val context: Context) {
    fun load(): Bitmap {
        return (ContextCompat.getDrawable(context, R.drawable.earth_mask) as BitmapDrawable).bitmap
    }
}

private class ClusteringViewModel(
    coroutineScope: CoroutineScope,
    earthMaskLoader: EarthMaskLoader
) {
    private data class Viewport(val rect: GeoRect, val zoom: Int)

    private val viewports = MutableStateFlow<Viewport?>(null)

    var loading by mutableStateOf(true)
        private set

    val objects: List<ObjectInfo> by flow {
        val mask = earthMaskLoader.load()
        val clustering = createClustering(mask)
        loading = false
        viewports.filterNotNull().collect {
            val objects = clustering.getObjects(it.rect, it.zoom)
            emit(objects)
        }
    }.toState(listOf(), coroutineScope)

    fun setViewport(rect: GeoRect, zoom: Float) {
        viewports.value = Viewport(rect, zoom.toInt())
    }
}

@Composable
fun ClusteringScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { ClusteringViewModel(coroutineScope, EarthMaskLoader(context)) }

    MapView(
        cameraState = rememberCameraState(
            CameraPosition(GeoPoint(49.545, 78.87), Zoom(2.0f))
        )
    ) {
        ViewportController(vm)
        Objects(vm.objects)
        if (vm.loading) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun ViewportController(vm: ClusteringViewModel) {
    val zoom = Camera.zoom
    val rect = if (zoom > 2.0f) Camera.visibleRect.extend(0.1) else WorldRect
    LaunchedEffect(vm, rect, zoom) {
        vm.setViewport(rect, zoom)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize()) {
        Text(
            "Loading...", style = TextStyle(Color.Red, fontSize = 30.sp), modifier = Modifier.align(
                Alignment.Center
            )
        )
    }
}

@Composable
private fun Objects(objects: List<ObjectInfo>) {
    val pinImage = imageFromResource(R.drawable.ic_pin)
    val crownImage = imageFromResource(R.drawable.ic_crown)
    val textStyle = TextStyle(Color.Black, fontSize = 12.sp)

    objects.forEach {
        key(it.position) {
            when (it) {
                is ClusterInfo -> {
                    val count = it.count
                    val color = when {
                        count < 10 -> Color(110, 200, 60)
                        count < 100 -> Color(240, 200, 20)
                        count < 1000 -> Color(240, 130, 20)
                        count < 10000 -> Color(250, 80, 80)
                        else -> Color(240, 50, 200)
                    }
                    val size = when {
                        count < 10 -> 20.dp
                        count < 100 -> 25.dp
                        count < 1000 -> 30.dp
                        count < 10000 -> 40.dp
                        else -> 50.dp
                    }
                    Point(it.position, size, color)
                    Label(it.position, "$count", style = textStyle)
                }
                is MarkerInfo -> {
                    val icon = if (it.isCrown) crownImage else pinImage
                    Marker(it.position, icon)
                }
            }
        }
    }
}