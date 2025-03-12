package com.example.hoverrobot.ui.settingsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hoverrobot.data.models.comms.CommandsRobot
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.comms.RobotLocalConfig
import com.example.hoverrobot.data.models.comms.Wheel
import com.example.hoverrobot.data.utils.StatusRobot
import com.example.hoverrobot.ui.settingsFragment.compose.OnActionSettingsScreen
import com.example.hoverrobot.ui.settingsFragment.compose.SettingsFragmentScreen

class SettingsFragment : Fragment() {

    private val settingsFragmentViewModel: SettingsFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        setContent {
            MaterialTheme {
                SettingsFragmentScreen(
                    initialRobotConfig = settingsFragmentViewModel.localConfigFromRobot.observeAsState(RobotLocalConfig()),
                    statusRobot = settingsFragmentViewModel.statusRobot.observeAsState().value ?: StatusRobot.ERROR_MCB_CONNECTION,
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
       return if(sendNewSetting(newSetting))
            settingsFragmentViewModel.sendCommand(CommandsRobot.SAVE_PARAMS_SETTINGS)
        else false
    }

    private fun sendNewSetting(newSetting: PidSettings): Boolean {
        return settingsFragmentViewModel.sendNewPidTunning(newSetting)
    }
}