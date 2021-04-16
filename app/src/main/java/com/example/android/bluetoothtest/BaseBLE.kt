package com.example.android.bluetoothtest

import android.util.Log

abstract class BaseBLE{

    /**
     * Turns off device's bluetooth
     */
    abstract fun turnOff()

    /**
     * Turns on device's bluetooth
     */
    abstract fun turnOn()

    fun showConnection(){
        Log.i("Padre", "Padre")
    }
}