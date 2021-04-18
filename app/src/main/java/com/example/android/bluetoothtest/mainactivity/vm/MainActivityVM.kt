package com.example.android.bluetoothtest.mainactivity.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.bluetoothtest.BTDevice

class MainActivityVM : ViewModel(){

    private var _btDeviceList = MutableLiveData<Set<BTDevice>>()
    val btDeviceList: LiveData<Set<BTDevice>>
        get() = _btDeviceList

    init {
        _btDeviceList.value = emptySet()
    }

    fun setBTDeviceList(btDeviceList: Set<BTDevice>){
        _btDeviceList.value = btDeviceList
    }

    fun mergeBTDeviceList(first: Set<BTDevice>?, second: Set<BTDevice>): Set<BTDevice> {
        return first?.plus(second)!!
    }

    fun addToBTDeviceList(list: Set<BTDevice>){
        _btDeviceList.value = mergeBTDeviceList(_btDeviceList.value, list)
    }

    override fun onCleared() {
        super.onCleared()
    }


}