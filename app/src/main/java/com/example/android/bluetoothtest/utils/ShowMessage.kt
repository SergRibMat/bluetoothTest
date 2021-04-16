package com.example.android.bluetoothtest.utils

import android.content.Context
import android.widget.Toast

fun showMessage(context: Context, text: String){
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun showMessage(context: Context, text: Int){
    Toast.makeText(context, "$text", Toast.LENGTH_SHORT).show()
}