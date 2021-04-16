package com.example.android.bluetoothtest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.bluetoothtest.databinding.ItemDeviceBinding

class AdapterDevice(    private var deviceList: List<BTDevice>?,
                        private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<AdapterDevice.ViewHolder>() {

    private lateinit var binding: ItemDeviceBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    //sets value to view objects
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        deviceList?.let {
            holder.tvName.text = "Name: ${it[position].name}"

            holder.tvAddress.text = "Adress: ${it[position].address}"

            holder.itemView.setOnClickListener { _ ->
                cellClickListener.onCellClickListener(it[position])
            }
        } ?: clearList()
    }

    override fun getItemCount(): Int {
        return deviceList?.size ?: 0
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val tvName = binding.tvDeviceName
        val tvAddress = binding.tvDeviceAddress
    }

    private fun clearList() {
        val emptyList = listOf<BTDevice>()
        deviceList = emptyList
        notifyItemRangeRemoved(0, 0)
    }
}

interface CellClickListener {
    fun onCellClickListener(btDevice: BTDevice)
}

data class BTDevice(
    val name: String,
    val address: String
)