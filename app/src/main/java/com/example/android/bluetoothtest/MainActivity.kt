package com.example.android.bluetoothtest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.example.android.bluetoothtest.databinding.ActivityMainBinding
import com.example.android.bluetoothtest.utils.showMessage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


const val MY_TAG: String = "MainActivity"
val TAG = "MY_APP_DEBUG_TAG"
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2



//After you have found a device to connect to, use cancelDiscovery()
//do not perform discovery while you are already connected to a device


class MainActivity : AppCompatActivity(), CellClickListener, BluetoothActionsInterface {

    private lateinit var viewModel: MainActivityVM

    var bluetoothHeadset: BluetoothHeadset? = null
    // Get the default adapter
    private lateinit var bleNative: BLENative

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        withDexter()

        viewModel = ViewModelProviders.of(this).get(MainActivityVM::class.java)

        bleNative = BLENative(this)

        //declare click listeners
        binding.tbStartBluetooth.setOnClickListener {
            if (binding.tbStartBluetooth.isChecked){
                activateBluetooth()
            }else{
                deactivateBluetooth()
            }
        }

        binding.btnCheckBluetooth.setOnClickListener {
            checkIfBluetoothEnabled()
        }

        binding.btnDiscoverable.setOnClickListener {
            bleNative.makeDeviceDiscoverable()
        }
        binding.btnDiscover.setOnClickListener {
            bleNative.discoveryDevices()
        }

        // Register for broadcasts
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiverActionFound, filter)

        val discoverableIntentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(receiverActionScanModeChanged, discoverableIntentFilter)

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiverActionStateChanged, intentFilter)

        //Declare Observers
        viewModel.btDeviceList.observe(this, {
            val adapter = AdapterDevice(it.toList(), this)
            binding.rvDeviceList.adapter = adapter
        })
    }

    fun withDexter(){

            Dexter.withContext(this)
                .withPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        showMessage(applicationContext, "Permission granted")
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        showMessage(applicationContext,"Permission denied")
                    }

                    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
                        showMessage(applicationContext,"onPermissionRationaleShouldBeShown")

                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Give Permission Internet")
                            .setMessage("You need to give permission internet for this app")
                            .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener {
                                    dialogInterface, i ->
                                dialogInterface.dismiss()
                                token?.cancelPermissionRequest()
                            })
                            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                                    dialogInterface, i ->
                                dialogInterface.dismiss()
                                token?.continuePermissionRequest()
                            })
                            .show()
                    }

                })
                .check()

        Dexter.withContext(this)
            .withPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    showMessage(applicationContext, "Permission granted")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    showMessage(applicationContext,"Permission denied")
                }

                override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
                    showMessage(applicationContext,"onPermissionRationaleShouldBeShown")

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Give Permission Internet")
                        .setMessage("You need to give permission internet for this app")
                        .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener {
                                dialogInterface, i ->
                            dialogInterface.dismiss()
                            token?.cancelPermissionRequest()
                        })
                        .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener {
                                dialogInterface, i ->
                            dialogInterface.dismiss()
                            token?.continuePermissionRequest()
                        })
                        .show()
                }

            })
            .check()
        }



    fun checkIfBluetoothEnabled(){
        if (bleNative.bluetoothEnabled()) {
            showMessage(this, "ENCENDIDO")
        }else{
            showMessage(this, "APAGADO")
        }
    }

    fun pairedDevices(){
        val pairedDevices = bleNative.pairedDevicesSet()
        if (pairedDevices.isNullOrEmpty()){
            Log.i(TAG, "There is no paired devices here")
        }else{
            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                Log.i(TAG, "Paired Device deviceName: $deviceName MAC: $deviceHardwareAddress")
            }
        }
    }

    override fun actionRequestDiscoverable(){
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)//5 minutes
        }
        startActivity(discoverableIntent)
    }

    private fun deactivateBluetooth(){
        if (!bleNative.isNull()){
            bleNative.turnOff()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister receivers.
        unregisterReceiver(receiverActionStateChanged)
        unregisterReceiver(receiverActionFound)
        unregisterReceiver(receiverActionScanModeChanged)
    }


    override fun onCellClickListener(btDevice: BTDevice) {
        showMessage(this, "${btDevice.name}, ${btDevice.address}")
    }

    override fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Create a BroadcastReceiver for ACTION_STATE_CHANGED.
    val receiverActionStateChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when(action) {
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

    //Create BroadcastReciever for ACTION_SCAN_MODE_CHANGED
    private val receiverActionScanModeChanged = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                val mode = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR
                )
                when (mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> Log.d(
                            TAG,
                            "recieverActionScanModeChanged: Discoverability Enabled."
                    )
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> Log.d(
                            TAG,
                            "recieverActionScanModeChanged: Discoverability Disabled. Able to receive connections."
                    )
                    BluetoothAdapter.SCAN_MODE_NONE -> Log.d(
                            TAG,
                            "recieverActionScanModeChanged: Discoverability Disabled. Not able to receive connections."
                    )
                    BluetoothAdapter.STATE_CONNECTING -> Log.d(
                            TAG,
                            "recieverActionScanModeChanged: Connecting...."
                    )
                    BluetoothAdapter.STATE_CONNECTED -> Log.d(
                            TAG,
                            "recieverActionScanModeChanged: Connected."
                    )
                }
            }
        }
    }

    //Create BroadcastReciever for ACTION_FOUND
    private val receiverActionFound: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val name = device?.name ?: "No Name Available"
                val address = device?.address ?: "No Address Available"
                Log.d(TAG, "onReceive: $name $address")
                viewModel.addToBTDeviceList(
                        setOf(BTDevice(name, address))
                )
            }
        }
    }

    override fun activateBluetooth(){
        if (!bleNative.isNull()) {
            if (!bleNative.isEnabled()) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 77)
            }
        }
    }

}