package me.tartikov.map.app

import androidx.compose.runtime.*

class Screen(val name: String, val content: @Composable () -> Unit)

class Router(private val homeScreen: Screen) {
    var screen by mutableStateOf(homeScreen)
        private set

    fun goHome(): Boolean {
        if (screen == homeScreen) {
            return false
        }
        screen = homeScreen
        return true
    }

    fun goTo(screen: Screen) {
        this.screen = screen
    }
}

val LocalRouter = staticCompositionLocalOf<Router> {
    error("Can't find Router")
}