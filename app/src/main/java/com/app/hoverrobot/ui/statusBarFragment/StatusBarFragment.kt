package com.app.hoverrobot.ui.statusBarFragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.hoverrobot.data.models.Battery
import com.app.hoverrobot.data.models.comms.ConnectionState
import com.app.hoverrobot.data.utils.StatusRobot

class StatusBarFragment : Fragment() {

    private val statusBarViewModel: StatusBarViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        setContent {
            StatusBarScreen(
                statusRobot = statusBarViewModel.statusRobot.observeAsState(StatusRobot.INIT),
                connectionState = statusBarViewModel.connectionState.observeAsState(ConnectionState()),
                tempImu = statusBarViewModel.tempImu.observeAsState(0F),
                batteryState = statusBarViewModel.battery.observeAsState(Battery())
            ) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
    }
}