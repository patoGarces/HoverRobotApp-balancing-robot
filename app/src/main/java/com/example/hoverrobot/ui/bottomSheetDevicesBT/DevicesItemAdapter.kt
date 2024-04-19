package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hoverrobot.R

class DevicesItemAdapter(private val devicesItem : List<BluetoothDevice>, private val onClickListener:(BluetoothDevice) -> Unit ) :
    RecyclerView.Adapter<DevicesItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DevicesItemViewHolder(layoutInflater.inflate(R.layout.item_device_bt,parent,false))
    }

    override fun onBindViewHolder(holder: DevicesItemViewHolder, position: Int) {
        val item = devicesItem[position]
        holder.render(item,onClickListener)
    }

    override fun getItemCount(): Int {
        return devicesItem.size
    }

}