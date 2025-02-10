package com.example.hoverrobot.ui.settingsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.Wheel
import com.example.hoverrobot.databinding.SettingsFragmentBinding
import com.google.android.material.slider.Slider


class SettingsFragment : Fragment() {

    private lateinit var _binding: SettingsFragmentBinding
    private val binding get() = _binding

    private val settingsFragmentViewModel: SettingsFragmentViewModel by activityViewModels()

    private var indexPidTarget = 0
    private var kpValue: Float = 0f
    private var kiValue: Float = 0f
    private var kdValue: Float = 0f
    private var centerValue: Float = 0f
    private var safetyLimitsValue: Float = 10f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupListener()
        setupObserver()
    }

    private fun setupSpinner() {
        binding.spTargetPid.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.sp_memory_items)
        )

        binding.spTargetPid.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                indexPidTarget = position
                Log.d("pidYaw","index: $position")
                loadSpinnersConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupListener() {

        binding.sbPidP.addOnChangeListener { _, progressP, _ ->
            kpValue = progressP
            binding.tvValueP.text = getString(R.string.value_slider_format).format((kpValue))
        }

        binding.sbPidP.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbPidI.addOnChangeListener { _, progressI, _ ->
            kiValue = progressI
            binding.tvValueI.text = getString(R.string.value_slider_format).format((kiValue))
        }

        binding.sbPidI.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbPidD.addOnChangeListener { _, progressD, _ ->
            kdValue = progressD
            binding.tvValueD.text = getString(R.string.value_slider_format).format((kdValue))
        }

        binding.sbPidD.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbCenterAngle.addOnChangeListener { _, progressCenter, _ ->
            centerValue = progressCenter
            binding.tvValueCenter.text = centerValue.toString()
        }

        binding.sbCenterAngle.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbSafetyLimits.addOnChangeListener { _, progressSafetyLimits, _ ->
            safetyLimitsValue = progressSafetyLimits
            binding.tvValueLimits.text = safetyLimitsValue.toString()
        }

        binding.sbSafetyLimits.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.btnSyncPid.setOnClickListener {
            Toast.makeText(requireContext(), "Sincronizando parametros", Toast.LENGTH_LONG).show()
            sendNewSetting()
        }

        binding.btnSavePid.setOnClickListener {
            saveLocalSettings()
        }

        binding.btnResetPid.setOnClickListener {
            loadSpinnersConfig()
            sendNewSetting()
        }

        binding.btnCalibrateImu.setOnClickListener {
            settingsFragmentViewModel.sendCommand(CommandsRobot.CALIBRATE_IMU)
        }

        binding.btnCleanWheelLeft.setOnClickListener {
            settingsFragmentViewModel.sendCommand(CommandsRobot.CLEAN_WHEELS, Wheel.LEFT_WHEEL.ordinal.toFloat())
        }

        binding.btnCleanWheelRight.setOnClickListener {
            settingsFragmentViewModel.sendCommand(CommandsRobot.CLEAN_WHEELS,Wheel.RIGHT_RIGHT.ordinal.toFloat())
        }
    }

    private fun setupObserver() {
        settingsFragmentViewModel.localConfigFromRobot.observe(viewLifecycleOwner) {
            it?.updateSpinners()
        }
    }

    private fun loadSpinnersConfig() {
        settingsFragmentViewModel.localConfigFromRobot.value?.updateSpinners()
    }

    private fun RobotLocalConfig.updateSpinners() {
        kpValue = this.pids[indexPidTarget].kp
        binding.sbPidP.inRange(kpValue)
        kiValue = this.pids[indexPidTarget].ki
        binding.sbPidI.inRange(kiValue)
        kdValue = this.pids[indexPidTarget].kd
        binding.sbPidD.inRange(kdValue)
        centerValue = this.centerAngle
        binding.sbCenterAngle.inRange(centerValue)
        safetyLimitsValue = this.safetyLimits
        binding.sbSafetyLimits.inRange(safetyLimitsValue)
        binding.btnSavePid.isEnabled = false
        binding.btnResetPid.isVisible = false
    }

    private fun saveLocalSettings() {
        sendNewSetting()
        val isSend = settingsFragmentViewModel.sendCommand(CommandsRobot.SAVE_PARAMS_SETTINGS)
        binding.btnSavePid.isEnabled = !isSend
        binding.btnResetPid.isVisible = !isSend
    }


    private fun sendNewSetting() {
        val newSetting =
            PidSettings(indexPidTarget, kpValue, kiValue, kdValue, centerValue, safetyLimitsValue)
        val isSend = settingsFragmentViewModel.sendNewPidTunning(newSetting)
        if (isSend) {
            val isDiff = newSetting.isDiffWithLastLocalConfig()
            binding.btnResetPid.isVisible = isDiff
            binding.btnSavePid.isEnabled = isDiff
        }
    }

    private fun Slider.inRange(value: Float) {
        val safeValue = (value * 100).toInt() / 100.00
        this.value = if (value in valueFrom..valueTo) {
            safeValue.toFloat()
        } else {
            valueTo
        }
    }

    private fun PidSettings.isDiffWithLastLocalConfig(): Boolean =
        settingsFragmentViewModel.localConfigFromRobot.value?.let { localConfig ->
            this.kp != localConfig.pids[this.indexPid].kp ||
                    this.ki != localConfig.pids[this.indexPid].ki ||
                    this.kd != localConfig.pids[this.indexPid].kd ||
                    this.centerAngle != localConfig.centerAngle ||
                    this.safetyLimits != localConfig.safetyLimits
        } ?: true

}