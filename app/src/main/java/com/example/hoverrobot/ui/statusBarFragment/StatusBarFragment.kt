package com.example.hoverrobot.ui.statusBarFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.hoverrobot.MainActivity
import com.example.hoverrobot.R
import com.example.hoverrobot.ToolBox
import com.example.hoverrobot.databinding.StatusBarFragmentBinding
import com.example.hoverrobot.data.utils.StatusMapperBT
import java.io.IOException


class StatusBarFragment : Fragment() {

    private var _binding : StatusBarFragmentBinding? =null
    private val binding get() = _binding!!

    private val statusBarViewModel: StatusBarViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var myFragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StatusBarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myFragmentManager = (activity as MainActivity).supportFragmentManager

        setupListener()
        setupObserver()
    }

    private fun setupObserver() {

        statusBarViewModel.connectionStatus.observe(viewLifecycleOwner){
            it?.let {
                binding.btnStatus.text = StatusMapperBT.mapStatusTostring(it,statusBarViewModel.statusRobot.value)
                ToolBox.changeStrokeColor(requireContext(),binding.btnStatus,
                    StatusMapperBT.mapStatusToColor(it,statusBarViewModel.statusRobot.value),3)
            }
        }

        statusBarViewModel.statusRobot.observe(viewLifecycleOwner){
            binding.btnStatus.text = StatusMapperBT.mapStatusTostring(statusBarViewModel.connectionStatus.value!!,it)
            ToolBox.changeStrokeColor(requireContext(),binding.btnStatus,
                StatusMapperBT.mapStatusToColor(statusBarViewModel.connectionStatus.value!!,it),3)
        }

        statusBarViewModel.battery.observe(viewLifecycleOwner) {

            it?.let{
                with(binding.ibBatteryStatus) {

                    if (it.batLevel in 101..199) {
                        setImageResource(R.drawable.ic_battery_charging)
                        binding.tvBatteryPercent.text = String.format(getString(R.string.placehoder_battery),(it.batLevel-100).toString())
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

                        binding.tvBatteryPercent.text = String.format(getString(R.string.placehoder_battery),it.batLevel.toString())
                    }
                }
            }
        }

        statusBarViewModel.fpsStatus.observe(viewLifecycleOwner){
            binding.tvStreamFps.text = String.format(binding.tvStreamFps.text.toString(),it)
        }

        statusBarViewModel.tempImu.observe(viewLifecycleOwner) {
            try {
                binding.tvTemperature.text =  String.format(getString(R.string.placehoder_temp), it)
            }
            catch (e : IOException){
                Log.e("statusBarFragment","rompio el cosito de temp",e)
            }
        }
    }

    private fun setupListener() {

        binding.btnStatus.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.statusDataFragment)
        }

        binding.btnStatus.setOnLongClickListener {
            statusBarViewModel.setShowDialogDevices( true )
            true
        }
    }

    companion object{
        const val BATTERY_EMPTY = 10
        const val BATTERY_LOW = 25
        const val BATTERY_MEDIUM = 60
        const val BATTERY_HIGH = 80
    }
}