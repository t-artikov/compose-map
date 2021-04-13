package me.tartikov.map.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.google.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity() {
    private val router = Router(Screen("Home") { HomeScreen() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content(router)
        }
    }

    override fun onBackPressed() {
        if (!router.goHome()) {
            super.onBackPressed()
        }
    }
}

@Composable
private fun Content(router: Router) {
    MaterialTheme {
        CompositionLocalProvider(
            LocalRouter provides router
        ) {
            ProvideWindowInsets {
                Crossfade(router.screen) {
                    it.content()
                }
            }
        }
    }
}
