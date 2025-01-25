package com.example.hoverrobot.ui.statusBarFragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.utils.ImagebuttonsMappers.strengthIconMapper
import com.example.hoverrobot.data.utils.StatusConnection
import com.example.hoverrobot.data.utils.StatusMapper.colorBtnStatusRes
import com.example.hoverrobot.data.utils.StatusMapper.stringBtnStatusRes
import com.example.hoverrobot.data.utils.ToolBox
import com.example.hoverrobot.databinding.StatusBarFragmentBinding

class StatusBarFragment : Fragment() {

    private var _binding : StatusBarFragmentBinding? =null
    private val binding get() = _binding!!

    private val statusBarViewModel: StatusBarViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StatusBarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObserver()

        binding.btnStatus.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun setupObserver() {
        statusBarViewModel.connectionState.observe(viewLifecycleOwner) {
            statusBarViewModel.statusRobot.value?.let { statusRobot ->
                binding.btnStatus.text =
                    getString(statusRobot.stringBtnStatusRes(it.status))
                ToolBox.changeStrokeColor(
                    binding.btnStatus,
                    requireContext().getColor(
                        statusRobot.colorBtnStatusRes(
                            it.status
                        )
                    ), 3
                )
            }

            binding.tvPacketsRate.text = String.format(getString(R.string.placeholder_packets_rate),it.receiverPacketRates)
            binding.ibStrength.setImageResource(strengthIconMapper(it.strength))
            binding.tvRssi.text = String.format(getString(R.string.placeholder_rssi),it.rssi)
        }

        statusBarViewModel.statusRobot.observe(viewLifecycleOwner) {
            it?.let { statusRobot ->
                statusBarViewModel.connectionState.value?.status?.let { statusConnection ->
                    binding.btnStatus.text =
                        getString(statusRobot.stringBtnStatusRes(statusConnection))
                    ToolBox.changeStrokeColor(
                        binding.btnStatus,
                        requireContext().getColor(
                            statusRobot.colorBtnStatusRes(statusConnection)
                        ), 3
                    )
                }
            }
        }

        statusBarViewModel.battery.observe(viewLifecycleOwner) {

            it?.let{
                binding.tvBatteryVoltage.text = String.format(getString(R.string.placeholder_battery_voltage),it.batVoltage)
                with(binding.ibBatteryStatus) {
                    if (statusBarViewModel.connectionState.value?.status != StatusConnection.CONNECTED) {
                        setImageResource(R.drawable.ic_battery_unknown)
                        binding.tvBatteryVoltage.text = "-.-v"
                        binding.tvBatteryPercent.text = ""
                        binding.ibBatteryStatus.imageTintList = null
                    }
                    else if (it.batLevel in 101..199) {
                        setImageResource(R.drawable.ic_battery_charging)
                        binding.tvBatteryPercent.text = String.format(getString(R.string.placeholder_battery_percent),(it.batLevel-100).toString())
                    }
                    else {
                        if (it.batLevel > BATTERY_HIGH) {
                            setImageResource(R.drawable.ic_battery_4)
                            binding.ibBatteryStatus.imageTintList = null
                        } else if (it.batLevel > BATTERY_MEDIUM) {
                            setImageResource(R.drawable.ic_battery_3)
                            binding.ibBatteryStatus.imageTintList = null
                        } else if (it.batLevel > BATTERY_LOW) {
                            setImageResource(R.drawable.ic_battery_2)
                            binding.ibBatteryStatus.imageTintList = null
                        } else if (it.batLevel > BATTERY_EMPTY) {
                            setImageResource(R.drawable.ic_battery_1)
                            binding.ibBatteryStatus.imageTintList = ColorStateList.valueOf(context.getColor(R.color.status_orange))
                        }
                        else{
                            setImageResource(R.drawable.ic_battery_0)
                            binding.ibBatteryStatus.imageTintList = ColorStateList.valueOf(context.getColor(R.color.red))
                        }
                        binding.tvBatteryPercent.text = String.format(getString(R.string.placeholder_battery_percent),it.batLevel.toString())
                    }
                }
            }
        }

        statusBarViewModel.tempImu.observe(viewLifecycleOwner) {
            binding.tvTemperature.text =  String.format(getString(R.string.placeholder_temp), it)
        }
    }

    companion object{
        const val BATTERY_EMPTY = 10
        const val BATTERY_LOW = 25
        const val BATTERY_MEDIUM = 60
        const val BATTERY_HIGH = 80
    }
}