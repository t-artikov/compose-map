package me.tartikov.map.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    val screens = listOf(
        Screen("Hello world") { HelloWorldScreen() },
        Screen("Drag") { DragScreen() },
        Screen("Animation") { AnimationScreen() },
        Screen("Overlay") { OverlayScreen() },
        Screen("Camera") { CameraScreen() },
        Screen("Zoom Dependent") { ZoomDependentScreen() },
        Screen("Clustering") { ClusteringScreen() },
    )
    val router = LocalRouter.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .align(Alignment.Center)
        ) {
            screens.forEach {
                Button(
                    onClick = { router.goTo(it) },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(it.name)
                }
            }
        }
    }
}
