package me.tartikov.map

import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.dgis.sdk.DGis
import ru.dgis.sdk.map.imageFromResource

@Composable
fun imageFromResource(@DrawableRes resourceId: Int) : Image {
    val value = remember { TypedValue() }
    LocalContext.current.resources.getValue(resourceId, value, true)
    val image = remember(value.string, resourceId) {
        imageFromResource(DGis.context(), resourceId)
    }
    DisposableEffect(image) {
        onDispose {
            image.close()
        }
    }
    return image
}
