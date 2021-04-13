package me.tartikov.map.app

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding
import me.tartikov.map.*

@Composable
fun HelloWorldScreen() {
    val context = LocalContext.current
    fun showMessage(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    var useCircle by remember { mutableStateOf(true) }

    MapView {
        Row(Modifier.systemBarsPadding().padding(4.dp)) {
            Text("Use Circle")
            Switch(checked = useCircle, onCheckedChange = {
                useCircle = it
            })
        }
        Marker(
            position = GeoPoint(55.76244998776792, 37.509797886013985),
            icon = imageFromResource(R.drawable.ic_marker),
            onClick = { showMessage("Marker clicked") }
        )
        Polyline(
            points = listOf(
                GeoPoint(55.83628521457583, 37.55877426825464),
                GeoPoint(55.77198968744181, 37.732498683035374),
                GeoPoint(55.63988274791271, 37.69672337919474)
            ),
            width = 3.dp,
            color = Color.Red
        )
        if (useCircle) {
            Circle(
                position = GeoPoint(55.71075872699374, 37.595025496557355),
                radius = 3000.meter,
                color = Color.Magenta.copy(alpha = 0.5f),
                strokeWidth = 1.dp,
                strokeColor = Color.Black,
                onClick = { showMessage("Circle clicked") }
            )
        } else {
            Polygon(
                contour = listOf(
                    GeoPoint(55.72398673527674, 37.519549978896976),
                    GeoPoint(55.75504116974128, 37.604301664978266),
                    GeoPoint(55.70775439843358, 37.647088933736086),
                    GeoPoint(55.65669468440851, 37.58866782300174)
                ),
                color = Color(255, 120, 20),
                strokeWidth = 1.dp,
                strokeColor = Color.Black,
                onClick = { showMessage("Polygon clicked") }
            )
        }
    }
}
