package me.tartikov.map
import ru.dgis.sdk.coordinates.Arcdegree
import kotlin.math.*

fun GeoPoint(latitude: Double, longitude: Double): GeoPoint {
    return GeoPoint(Arcdegree(latitude), Arcdegree(longitude))
}

val GeoPoint.lat get() = latitude.value
val GeoPoint.lon get() = longitude.value

fun distance(p1: GeoPoint, p2: GeoPoint): Meter {
    fun toRadians(degree: Double): Double {
        return degree * Math.PI / 180.0
    }

    val dLat = toRadians(p2.lat - p1.lat)
    val dLon = toRadians(p2.lon - p1.lon)
    val lat1 = toRadians(p1.lat)
    val lat2 = toRadians(p2.lat)

    val r = 6378136.5
    val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
    val c = 2 * asin(sqrt(a))
    return Meter((r * c).toFloat())
}
