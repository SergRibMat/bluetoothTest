package com.example.android.bluetoothtest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.polidea.rxandroidble2.scan.ScanSettings
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable


class BLELibrary(private val context: Context){

    private var rxBleClient: RxBleClient = RxBleClient.create(context)



    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            Log.d(TAG, "onReceive: primer broadcast reciever")
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.d(TAG, "onReceive: $deviceName: $deviceHardwareAddress")

                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, " ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, " ACTION_DISCOVERY_FINISHED")
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1111111)

                    when (state) {
                        BluetoothAdapter.STATE_OFF -> Log.d(TAG, "onReceive: STATE OFF")
                        BluetoothAdapter.STATE_TURNING_OFF -> Log.d(
                                TAG,
                                "receiver: STATE TURNING OFF"
                        )
                        BluetoothAdapter.STATE_ON -> Log.d(TAG, "receiver: STATE ON")
                        BluetoothAdapter.STATE_TURNING_ON -> Log.d(
                                TAG,
                                "receiver: STATE TURNING ON"
                        )
                    }

                }
            }
        }
    }

    fun scanDevices(){
        Log.i("NewBLELibrary", "Class executed")
        val scanSubscription: Disposable = rxBleClient.scanBleDevices(
                ScanSettings.Builder() // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build() // add filters if needed
        )
                .subscribe(
                        { scanResult ->
                            Log.i("NewBLELibrary", "${scanResult.bleDevice.name}, ${scanResult.bleDevice.macAddress}")
                        }
                ) { throwable ->
                    Log.i("NewBLELibrary", "${throwable.message}")
                }

// When done, just dispose.

// When done, just dispose.
        //scanSubscription.dispose()
    }


}