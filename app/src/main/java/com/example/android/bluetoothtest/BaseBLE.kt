package com.example.android.bluetoothtest

import android.util.Log

abstract class BaseBLE: BaseBLEInterface{

    fun showConnection(){
        Log.i("Padre", "Padre")
    }
}