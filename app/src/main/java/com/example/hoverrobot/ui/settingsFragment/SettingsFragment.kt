package com.example.hoverrobot.ui.settingsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.R
import com.example.hoverrobot.dataStore
import com.example.hoverrobot.databinding.SettingsFragmentBinding
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Math.round
import kotlin.math.truncate


class SettingsFragment : Fragment() {

    private var lastPidSettings : PidSettings? = null

    private lateinit var _binding : SettingsFragmentBinding
    private val binding get()= _binding

    val settingsFragmentViewModel : SettingsFragmentViewModel by viewModels(ownerProducer = { requireActivity() })

    private var kpValue: Float = 0f
    private var kiValue: Float = 0f
    private var kdValue: Float = 0f
    private var centerValue: Float = 0f
    private var safetyLimitsValue: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListener()
        setupObserver()
        getParametersFromStore()
    }

    private fun setupListener(){

        binding.sbPidP.addOnChangeListener { _, progressP, _ ->
            kpValue = progressP
            binding.tvValueP.text = getString(R.string.value_slider_format).format((kpValue))
        }

        binding.sbPidP.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbPidI.addOnChangeListener { _, progressI, _ ->
            kiValue = progressI
            binding.tvValueI.text = getString(R.string.value_slider_format).format((kiValue))
        }

        binding.sbPidI.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbPidD.addOnChangeListener { _, progressD, _ ->
            kdValue = progressD
            binding.tvValueD.text = getString(R.string.value_slider_format).format((kdValue))
        }

        binding.sbPidD.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbCenterAngle.addOnChangeListener { _, progressCenter, _ ->
            centerValue = progressCenter
            binding.tvValueCenter.text = centerValue.toString()
        }

        binding.sbCenterAngle.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.sbSafetyLimits.addOnChangeListener { _, progressSafetyLimits, _ ->
            safetyLimitsValue = progressSafetyLimits
            binding.tvValueLimits.text = safetyLimitsValue.toString()
        }

        binding.sbSafetyLimits.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) { }
            override fun onStopTrackingTouch(slider: Slider) {
                sendNewSetting()
            }
        })

        binding.btnSavePid.setOnClickListener {
            Toast.makeText(requireContext(),"Guardando parametros",Toast.LENGTH_LONG).show()
            saveParameters(PidSettings(kpValue,kiValue,kdValue,centerValue,safetyLimitsValue))
        }

        binding.btnGetPid.setOnClickListener {
            Toast.makeText(requireContext(),"Obteniendo parametros",Toast.LENGTH_LONG).show()
            getParametersFromStore()
        }

        binding.btnSyncPid.setOnClickListener {
            Toast.makeText(requireContext(),"Sincronizando parametros",Toast.LENGTH_LONG).show()
            sendNewSetting()
        }

        binding.btnCalibrateImu.setOnClickListener {
            settingsFragmentViewModel.sendCalibrateImu()            // TODO: crear dialog,esperar callback de confirmacion
        }
    }

    private fun sendNewSetting() {
        settingsFragmentViewModel.setPidTunningToRobot(PidSettings(kpValue,kiValue,kdValue,centerValue,safetyLimitsValue))
        binding.btnSyncPid.isEnabled = false
        binding.pbSyncLoading.isVisible = true
        lastPidSettings = null
    }

    private fun setupObserver(){
        settingsFragmentViewModel.pidSettingFromRobot.observe(viewLifecycleOwner) {
            it?.let {
                if (it != lastPidSettings) {
                    binding.sbPidP.inRange(it.kp)
                    binding.sbPidI.inRange(it.ki)
                    binding.sbPidD.inRange(it.kd)
                    binding.sbCenterAngle.inRange(it.centerAngle)
                    binding.sbSafetyLimits.inRange(it.safetyLimits)
                    lastPidSettings = settingsFragmentViewModel.pidSettingFromRobot.value
                    binding.btnSyncPid.isEnabled = true
                    binding.pbSyncLoading.isVisible = false
                }
            }
        }
    }

    private fun saveParameters(pidSettings: PidSettings) {   // TODO: agregar el getConnectedDeviceName para tener parametros diferenciados
        lifecycleScope.launch(Dispatchers.IO) {

            Log.d("SettingsFragment", KEY_PID_PARAM_CENTER.getDeviceParams)     // TODO: implementar
            context?.dataStore?.edit { settingsKey ->
                settingsKey[floatPreferencesKey(KEY_PID_PARAM_P)] = pidSettings.kp
                settingsKey[floatPreferencesKey(KEY_PID_PARAM_I)] = pidSettings.ki
                settingsKey[floatPreferencesKey(KEY_PID_PARAM_D)] = pidSettings.kd
                settingsKey[floatPreferencesKey(KEY_PID_PARAM_CENTER)] = pidSettings.centerAngle
                settingsKey[floatPreferencesKey(KEY_PID_PARAM_SAFETY_LIM)] = pidSettings.safetyLimits
            }
        }
    }

    private fun getParametersFromStore() {
        lifecycleScope.launch(Dispatchers.IO) {
            with(context?.dataStore?.data) {
                val paramP = this?.map { it[floatPreferencesKey(KEY_PID_PARAM_P)] }?.first()
                val paramI = this?.map { it[floatPreferencesKey(KEY_PID_PARAM_I)] }?.first()
                val paramD = this?.map { it[floatPreferencesKey(KEY_PID_PARAM_D)] }?.first()
                val paramCenter = this?.map { it[floatPreferencesKey(KEY_PID_PARAM_CENTER)] }?.first()
                val safetyLimits = this?.map { it[floatPreferencesKey(KEY_PID_PARAM_SAFETY_LIM)] }?.first()


                paramP?.let { binding.sbPidP.inRange(it) }
                paramI?.let { binding.sbPidI.inRange(it) }
                paramD?.let { binding.sbPidD.inRange(it) }
                paramCenter?.let { binding.sbCenterAngle.inRange(it) }
                safetyLimits?.let { binding.sbSafetyLimits.inRange(it) }
            }
        }
    }

    private fun Slider.inRange(value: Float) {
        val safeValue = (value * 100).toInt() / 100.00
        this.value = if( value in valueFrom..valueTo) { safeValue.toFloat() } else { valueFrom }
    }

    private val String.getDeviceParams: String
        get() = "${settingsFragmentViewModel.getDeviceConnectedName()}_${this}"
}

private const val KEY_PID_PARAM_P = "KEY_PID_P"
private const val KEY_PID_PARAM_I = "KEY_PID_I"
private const val KEY_PID_PARAM_D = "KEY_PID_D"
private const val KEY_PID_PARAM_CENTER = "KEY_PID_CENTER"
private const val KEY_PID_PARAM_SAFETY_LIM = "KEY_PID_SAFETY"