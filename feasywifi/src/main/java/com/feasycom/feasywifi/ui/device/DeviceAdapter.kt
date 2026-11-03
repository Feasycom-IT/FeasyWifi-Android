package com.feasycom.feasywifi.ui.device

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feasycom.feasywifi.databinding.ItemDeviceBinding
import com.feasycom.bean.BluetoothDeviceBwWrapper

class DeviceAdapter(private val mDevices: MutableList<BluetoothDeviceBwWrapper>) :
    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    var mOnItemClickListener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount() = mDevices.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = mDevices[position]
        holder.binding.run {
            tvName.text = if (device.locationName.isNullOrEmpty()) {
                if (device.name.isNullOrEmpty()) {
                    "Unknown"
                } else {
                    device.name
                }
            } else {
                device.locationName
            }
            tvAddr.text = device.address ?: "Unknown address"

            val progress = when {
                device.rssi <= -100 -> {
                    -100
                }

                device.rssi > 0 -> {
                    0
                }

                else -> {
                    device.rssi
                }
            }
            pbRssi.progress = 100 + progress
            tvRssi.text = "RSSI:${device.rssi}"
            root.setOnClickListener {
                mOnItemClickListener?.invoke(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.binding.run {
                mDevices[position].let {
                    val progress = when {
                        it.rssi <= -100 -> {
                            -100
                        }

                        it.rssi > 0 -> {
                            0
                        }

                        else -> {
                            it.rssi
                        }
                    }
                    pbRssi.progress = 100 + progress
                    tvRssi.text = "RSSI:${it.rssi}"
                    root.setOnClickListener {
                        mOnItemClickListener?.invoke(position)
                    }
                }
            }
        }
    }

    class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "DeviceAdapter"
    }
}