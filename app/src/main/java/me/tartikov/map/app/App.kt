package me.tartikov.map.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import ru.dgis.sdk.DGis
import ru.dgis.sdk.ApiKeys

private fun initDgis(context: Context) {
    val keys = ApiKeys(
        map = BuildConfig.MAP_API_KEY,
        directory = ""
    )
    DGis.initialize(context, keys)
}
@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initDgis(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
