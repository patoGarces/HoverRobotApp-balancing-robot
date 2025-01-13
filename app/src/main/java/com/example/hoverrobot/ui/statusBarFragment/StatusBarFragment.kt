package com.example.hoverrobot.ui.statusBarFragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.utils.StatusMapper
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

        statusBarViewModel.connectionStatus.observe(viewLifecycleOwner){
            it?.let {
                binding.btnStatus.text = StatusMapper.statusToString(it,statusBarViewModel.statusRobot.value)
                ToolBox.changeStrokeColor(binding.btnStatus,
                    requireContext().getColor(StatusMapper.statusToColor(it,statusBarViewModel.statusRobot.value)),3)
            }
        }

        statusBarViewModel.statusRobot.observe(viewLifecycleOwner){
            binding.btnStatus.text = StatusMapper.statusToString(statusBarViewModel.connectionStatus.value!!,it)
            ToolBox.changeStrokeColor(binding.btnStatus,
                requireContext().getColor(StatusMapper.statusToColor(statusBarViewModel.connectionStatus.value!!,it)),3)
        }

        statusBarViewModel.battery.observe(viewLifecycleOwner) {

            it?.let{
                binding.tvBatteryVoltage.text = String.format(getString(R.string.placeholder_battery_voltage),it.batVoltage)
                with(binding.ibBatteryStatus) {
                    if (it.batLevel in 101..199) {
                        setImageResource(R.drawable.ic_battery_charging)
                        binding.tvBatteryPercent.text = String.format(getString(R.string.placeholder_battery_percent),(it.batLevel-100).toString())
                    }
                    else {
                        if (it.batLevel > BATTERY_HIGH) {
                            setImageResource(R.drawable.ic_battery_4)
                        } else if (it.batLevel > BATTERY_MEDIUM) {
                            setImageResource(R.drawable.ic_battery_3)
                        } else if (it.batLevel > BATTERY_LOW) {
                            setImageResource(R.drawable.ic_battery_2)
                        } else if (it.batLevel > BATTERY_EMPTY) {
                            setImageResource(R.drawable.ic_battery_1)
                        }
                        else{
                            setImageResource(R.drawable.ic_battery_0)
                        }
                        binding.tvBatteryPercent.text = String.format(getString(R.string.placeholder_battery_percent),it.batLevel.toString())
                    }
                }
            }
        }

        statusBarViewModel.fpsStatus.observe(viewLifecycleOwner){
            binding.tvStreamFps.text = String.format(binding.tvStreamFps.text.toString(),it)
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