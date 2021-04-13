package me.tartikov.map

val Int.meter
    get() = Meter(this.toFloat())
