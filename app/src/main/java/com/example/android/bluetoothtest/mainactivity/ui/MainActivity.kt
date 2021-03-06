package com.example.android.bluetoothtest.mainactivity.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.android.bluetoothtest.*
import com.example.android.bluetoothtest.databinding.ActivityMainBinding
import com.example.android.bluetoothtest.mainactivity.vm.MainActivityVM
import com.example.android.bluetoothtest.utils.askForPermissionWithDexter
import com.example.android.bluetoothtest.utils.checkPermission
import com.example.android.bluetoothtest.utils.showMessage
import com.polidea.rxandroidble2.RxBleDevice


val TAG = "MY_APP_DEBUG_TAG"

class MainActivity : AppCompatActivity(), CellClickListener, BluetoothActionsInterface {

    private lateinit var viewModel: MainActivityVM
    lateinit var binding: ActivityMainBinding

    var bluetoothHeadset: BluetoothHeadset? = null
    // Get the default adapter
    private lateinit var bleNative: BLENative

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askForPermissionWithDexter(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "Fine Location")


        askForPermissionWithDexter(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                "Coarse Location")

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

        setBottomNavigationListeners()

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

    fun setBottomNavigationListeners(){
        binding.bottomNavigation.setOnNavigationItemSelectedListener {item ->
            actionsToBottomNavigationButtons(item)
        }

        //detecting when navigation items have been reselected:
        binding.bottomNavigation.setOnNavigationItemReselectedListener { item ->
                actionsToBottomNavigationButtons(item)
        }
    }

    private fun actionsToBottomNavigationButtons(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.menu_check_if_available -> {
                checkIfBluetoothEnabled()
                true
            }
            R.id.menu_discover -> {
                bleNative.discoveryDevices()
                true
            }
            R.id.menu_discoverable -> {
                bleNative.makeDeviceDiscoverable()
                true
            }
            else -> false
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

    override fun checkPermissionMainActivity(permission: String): Boolean {
        return checkPermission(this, permission)
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
                addDeviceToViewModelSet(device)
            }
        }
    }

    override fun addDeviceToViewModelSet(device: BluetoothDevice?){
        val name = device?.name ?: "No Name Available"
        val address = device?.address ?: "No Address Available"
        setParametersAndAddToViewModel(name, address)
    }

    override fun addDeviceToViewModelSet(device: RxBleDevice?){
        val name = device?.name ?: "No Name Available"
        val address = device?.macAddress ?: "No Address Available"
        setParametersAndAddToViewModel(name, address)
    }

    fun setParametersAndAddToViewModel(name: String, address: String){
        Log.d(TAG, "onReceive: $name $address")
        viewModel.addToBTDeviceList(
                setOf(BTDevice(name, address))
        )
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