package com.example.hoverrobot.ui.settingsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.PidParams
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.Wheel
import com.example.hoverrobot.databinding.SettingsFragmentBinding
import com.example.hoverrobot.ui.settingsFragment.compose.OnActionSettingsScreen
import com.example.hoverrobot.ui.settingsFragment.compose.SettingsFragmentScreen
import com.google.android.material.slider.Slider


class SettingsFragment : Fragment() {

    private val settingsFragmentViewModel: SettingsFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        setContent {
            MaterialTheme {
                val localConfig =
                    RobotLocalConfig(
                        pids = listOf(
                            PidParams(0.1f, 0.2f, 0.3f),
                            PidParams(0.4f, 0.5f, 0.6f),
                            PidParams(0.7f, 0.8f, 0.9f),
                            PidParams(1.2f, 1.4f, 1.6f)
                        ),
                        centerAngle = 0f,
                        safetyLimits = 5f
                    )

                SettingsFragmentScreen(
                    initialRobotConfig = settingsFragmentViewModel.localConfigFromRobot.observeAsState(localConfig),
                    onPidSave = ::saveLocalSettings,
                    onActionScreen = { onAction ->
                        when (onAction) {
                            is OnActionSettingsScreen.OnNewSettings -> {
                                Log.i("NewSettings","robotLocalConfig: ${onAction.pidSettings}")
                            }

                            is OnActionSettingsScreen.OnCalibrateImu -> settingsFragmentViewModel.sendCommand(CommandsRobot.CALIBRATE_IMU)

                            is OnActionSettingsScreen.OnCleanRightMotor -> settingsFragmentViewModel.sendCommand(CommandsRobot.CLEAN_WHEELS,Wheel.RIGHT_WHEEL.ordinal.toFloat())

                            is OnActionSettingsScreen.OnCleanLeftMotor -> settingsFragmentViewModel.sendCommand(CommandsRobot.CLEAN_WHEELS,Wheel.LEFT_WHEEL.ordinal.toFloat())
                        }
                    }
                )
            }
        }
    }

    private fun saveLocalSettings(newSetting: PidSettings): Boolean {
        Log.i("NewSettings","SAVE PID SETTINGS COMMANDS")
       return if(sendNewSetting(newSetting))
            settingsFragmentViewModel.sendCommand(CommandsRobot.SAVE_PARAMS_SETTINGS)
        else false
    }

//    private fun sendNewSetting(newSetting: PidSettings): Boolean {
//        Log.i("NewSettings","SEND PID SETTINGS COMMANDS")
//        return if (settingsFragmentViewModel.sendNewPidTunning(newSetting)) {
//            newSetting.isDiffWithLastLocalConfig()
//        } else false
//    }

    private fun sendNewSetting(newSetting: PidSettings): Boolean {
        Log.i("NewSettings","SEND PID SETTINGS COMMANDS")
        return settingsFragmentViewModel.sendNewPidTunning(newSetting)
    }
}