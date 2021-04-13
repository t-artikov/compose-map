package me.tartikov.map

import kotlin.math.max
import kotlin.math.min

val GeoRect.minLat get() = southWestPoint.latitude.value
val GeoRect.minLon get() = southWestPoint.longitude.value
val GeoRect.maxLat get() = northEastPoint.latitude.value
val GeoRect.maxLon get() = northEastPoint.longitude.value
val GeoRect.latSize get() = maxLat - minLat
val GeoRect.lonSize
    get() = (maxLon - minLon).let {
        if (it < 0.0) it + 360.0 else it
    }

fun GeoRect.contains(point: GeoPoint): Boolean {
    val containsLon = if (minLon <= maxLon) {
        point.lon in minLon..maxLon
    } else {
        point.lon >= minLon || point.lon <= maxLon
    }
    return containsLon && point.lat in minLat..maxLat
}

fun GeoRect.clamp(point: GeoPoint): GeoPoint {
    return GeoPoint(
        max(minLat, min(point.lat, maxLat)),
        max(minLon, min(point.lon, maxLon))
    )
}

fun GeoRect.extend(amount: Double): GeoRect {
    val latAmount = latSize * amount * 0.5
    val lonAmount = lonSize * amount * 0.5
    val newMinLat = max(minLat - latAmount, WorldRect.minLat)
    val newMaxLat = min(maxLat + latAmount, WorldRect.maxLat)
    val newMinLon = (minLon - lonAmount).let { if (it < WorldRect.minLon) it + 360 else it }
    val newMaxLon = (maxLon + lonAmount).let { if (it > WorldRect.maxLon) it - 360 else it }
    return GeoRect(GeoPoint(newMinLat, newMinLon), GeoPoint(newMaxLat, newMaxLon))
}

val WorldRect = GeoRect(GeoPoint(-90.0, -180.0), GeoPoint(90.0, 180.0))
