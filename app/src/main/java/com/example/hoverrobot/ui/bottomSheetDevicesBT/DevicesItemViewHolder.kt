package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.hoverrobot.databinding.ItemDeviceBtBinding

class DevicesItemViewHolder(view: View) : RecyclerView.ViewHolder(view){

    val binding = ItemDeviceBtBinding.bind(view)

    fun render(deviceItem: BluetoothDevice , onClickListener: (BluetoothDevice) -> Unit){

        if(deviceItem.name == null){
            binding.tvDeviceName.text = "Desconocido"
        }
        else {
            binding.tvDeviceName.text = deviceItem.name
        }
        binding.tvDeviceMac.text = deviceItem.address


        when(deviceItem.bondState){
            BluetoothDevice.BOND_NONE -> {
                binding.tvDeviceStatus.text = "Esperando"
            }
            BluetoothDevice.BOND_BONDED -> {
                binding.tvDeviceStatus.text = "Emparejado"
            }
            BluetoothDevice.BOND_BONDING -> {
                binding.tvDeviceStatus.text = "Emparejando"
            }
        }

        binding.root.setOnClickListener {
            onClickListener(deviceItem)
        }
    }
}