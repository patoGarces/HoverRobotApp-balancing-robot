package com.app.hoverrobot.ui.statusDataFragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.hoverrobot.BuildConfig
import com.app.hoverrobot.R
import com.app.hoverrobot.data.utils.StatusConnection
import com.app.hoverrobot.data.utils.StatusRobot
import com.app.hoverrobot.ui.navigationFragment.NavigationViewModel
import com.app.hoverrobot.ui.statusDataFragment.compose.StatusDataScreen

class StatusDataFragment : Fragment() {

    private val statusDataViewModel: StatusDataViewModel by viewModels(ownerProducer = { requireActivity() })
    private val navigationViewModel: NavigationViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
            setContent {
            MaterialTheme {
                StatusDataScreen(
                    statusRobot = statusDataViewModel.gralStatus.observeAsState().value ?: StatusRobot.ERROR,
                    statusConnection = statusDataViewModel.statusConnection.observeAsState().value ?: StatusConnection.ERROR,
                    defaultAggressiveness = navigationViewModel.aggressivenessLevel.observeAsState().value ?: 0,
                    mainboardTemp = statusDataViewModel.mainboardTemp.observeAsState().value,
                    motorControllerTemp =  statusDataViewModel.motorControllerTemp.observeAsState().value,
                    imuTemp =  statusDataViewModel.imuTemp.observeAsState().value,
                    version = getString(R.string.version_placeholder,BuildConfig.VERSION_NAME),
                    localIp = statusDataViewModel.localIp.observeAsState().value,
                    onOpenNetworkSettings = { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onAggressivenessChange = {
                        navigationViewModel.setLevelAggressiveness(it)
                    }
                )
            }
        }
    }
}