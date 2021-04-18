package com.example.android.bluetoothtest.remote.bluetooth.library

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android.bluetoothtest.BluetoothActionsInterface
import com.example.android.bluetoothtest.TAG
import com.polidea.rxandroidble2.scan.ScanSettings
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable


class BLELibrary(private val context: Context,
                 private val bluetoothActionsInterface: BluetoothActionsInterface
){

    private var rxBleClient: RxBleClient = RxBleClient.create(context)

    private var scanSubscription: Disposable? = null


    fun scanDevices(){
        Log.i("NewBLELibrary", "Class executed")
        scanSubscription = rxBleClient.scanBleDevices(
                ScanSettings.Builder() // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
        )
                .subscribe(
                        { scanResult ->
                            Log.i("NewBLELibrary", "${scanResult.bleDevice.name}, ${scanResult.bleDevice.macAddress}")
                            bluetoothActionsInterface.addDeviceToViewModelSet(scanResult.bleDevice)
                        }
                ) { throwable ->
                    Log.i("NewBLELibrary", "${throwable.message}")
                }
    }

    fun scanDispose(){
        scanSubscription?.dispose()
    }



}