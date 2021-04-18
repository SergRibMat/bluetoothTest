package com.example.android.bluetoothtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.example.android.bluetoothtest.utils.showMessage
import com.polidea.rxandroidble2.RxBleDevice

class BLENative(private val bluetoothActionsInterface: BluetoothActionsInterface) : BaseBLE() {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun turnOff() {
        bluetoothAdapter.disable()
    }

    override fun turnOn() {
        bluetoothActionsInterface.activateBluetooth()
    }

    fun isEnabled(): Boolean = bluetoothAdapter.isEnabled

    fun isNull(): Boolean = bluetoothAdapter == null

    /**
     * Starts Discovery for near bluetooth Devices
     * The devices will be heared by the correnpinding BroadcastReceiver
     */
    fun discoveryDevices(){
        if (!bluetoothAdapter.isDiscovering) {
            //check BT permissions in manifest
            if (permissionsForDiscoveryGranted()){
                bluetoothAdapter.startDiscovery()
                Log.i("BLENative", "Executted")
            }
        }
    }

    fun pairedDevicesSet() = bluetoothAdapter?.bondedDevices

    fun makeDeviceDiscoverable(){
        bluetoothActionsInterface.actionRequestDiscoverable()
    }

    private fun permissionsForDiscoveryGranted(): Boolean{
        return  (bluetoothActionsInterface.checkPermissionMainActivity(Manifest.permission.ACCESS_FINE_LOCATION)
                && bluetoothActionsInterface.checkPermissionMainActivity(Manifest.permission.ACCESS_FINE_LOCATION)        )
    }

    fun bluetoothEnabled(): Boolean{
        if(bluetoothAdapter == null){
            return false
        }
        return bluetoothAdapter.isEnabled
    }

}

interface BluetoothActionsInterface{
    fun activateBluetooth()
    fun checkPermissionMainActivity(permission: String): Boolean
    fun actionRequestDiscoverable()
    fun addDeviceToViewModelSet(device: BluetoothDevice?)
    fun addDeviceToViewModelSet(device: RxBleDevice?)
}



