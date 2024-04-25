package com.example.hoverrobot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hoverrobot.Models.comms.Battery
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.repository.CommsRepository
import com.example.hoverrobot.data.repository.CommsRepositoryImpl.Companion.HEADER_PACKET
import com.example.hoverrobot.data.utils.ConnectionStatus
import com.example.hoverrobot.data.utils.StatusEnumRobot
import com.example.hoverrobot.databinding.ActivityMainBinding
import com.example.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.example.hoverrobot.ui.analisisFragment.AnalisisViewModel
import com.example.hoverrobot.ui.bottomSheetDevicesBT.BottomSheetDevicesFragment
import com.example.hoverrobot.ui.bottomSheetDevicesBT.BottomSheetDevicesViewModel
import com.example.hoverrobot.ui.controlFragment.ControlFragment
import com.example.hoverrobot.ui.controlFragment.ControlViewModel
import com.example.hoverrobot.ui.settingsFragment.SettingsFragment
import com.example.hoverrobot.ui.settingsFragment.SettingsFragmentViewModel
import com.example.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.example.hoverrobot.ui.statusDataFragment.StatusDataViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val statusBarViewModel: StatusBarViewModel by viewModels()
    private val statusDataViewModel: StatusDataViewModel by viewModels()
    private val controlViewModel: ControlViewModel by viewModels()
    private val analisisViewModel: AnalisisViewModel by viewModels()
    private val settingsFragmentViewModel: SettingsFragmentViewModel by viewModels()
    private val bottomSheetDevicesViewModel: BottomSheetDevicesViewModel by viewModels()

    @Inject
    private lateinit var commsRepository: CommsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        commsRepository = CommsRepository(this)

        commsRepository.helloWorld()
        getPermissions()

//        webViewSetup()

        setupObservers()
        setupViewPagerAndTabLayout()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun setupViewPagerAndTabLayout() {

        binding.viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_transmision)
                1 -> tab.text = getString(R.string.tab_analisis)
                2 -> tab.text = getString(R.string.tab_settings)
            }
        }.attach()
        binding.viewPager.isUserInputEnabled = false

    }

    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ControlFragment()
                1 -> AnalisisFragment()
                2 -> SettingsFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }

    private fun setupObservers() {

        settingsFragmentViewModel.pidSettingToRobot.observe(this) {
            it?.let {
                if (commsRepository.connectionStateFlow.value == ConnectionStatus.CONNECTED) {
                    commsRepository.sendPidParams(it)
                } else {
                    Log.d("activity", "No se puede enviar configuración")
                    Toast.makeText(this, "Debe conectarse al bluetooth!", Toast.LENGTH_LONG).show()
                }
            }
        }

        bottomSheetDevicesViewModel.deviceSelected.observe(this) {
            it?.let {
                commsRepository.connectDevice(it)
            }
        }

        statusBarViewModel.connectionStatus.observe(this) {

            Log.d("activity", "connetionStatus changed: $it")
            it?.let {
                when (it) {
                    ConnectionStatus.INIT,
                    ConnectionStatus.DISCONNECT -> {
                        showDevicesToConnect()
                    }

                    ConnectionStatus.CONNECTED -> {
//                        binding.btnTest.isEnabled = true
                        controlViewModel.setVisibility(true)
                    }

                    ConnectionStatus.ERROR -> {
                        Log.d("connectionStatus", "STATUS_ERROR")
                    }

                    ConnectionStatus.DISCOVERING,
                    ConnectionStatus.CONNECTING -> {} // TODO()
                }
            }
        }

        statusBarViewModel.showDialogDevices.observe(this) {
            if (it == true) {
                showDevicesToConnect()
                statusBarViewModel.setShowDialogDevices(false)
            }
        }

        controlViewModel.controlAxis.observe(this) {
            it?.let {
                commsRepository.sendJoystickUpdate(it)
            }
        }

        lifecycleScope.launch {
            commsRepository.availableDevices.collect {
                bottomSheetDevicesViewModel.updateDevicesList(it)
            }
        }

        lifecycleScope.launch {
            commsRepository.connectionStateFlow.collect {
                statusBarViewModel.setConnectionStatus(it)
                bottomSheetDevicesViewModel.updateStatusBtnRefresh(it)
            }
        }

        lifecycleScope.launch {
            commsRepository.statusRobotFlow.collect { newStatus ->
                if (newStatus.header == HEADER_PACKET.toShort()) {

                    statusBarViewModel.setTempImu((newStatus.tempUcMain.toFloat() / 10))

                    val battery = Battery(
                        newStatus.batPercent.toInt(),
                        newStatus.batVoltage.toFloat(),
                        newStatus.batTemp.toFloat() / 10
                    )
                    statusBarViewModel.setBatteryStatus(battery)

                    StatusEnumRobot.values().getOrNull(newStatus.statusCode.toInt())
                    ?.let { statusBarViewModel.setStatusRobot(it) }

                    statusDataViewModel.setEscsTemp(newStatus.tempUcControl.toFloat() / 10)

                    statusDataViewModel.setImuTemp(newStatus.tempUcMain.toFloat() / 10)

                    statusDataViewModel.setGralStatus(newStatus.statusCode)

                    analisisViewModel.addNewPointData(newStatus)

                    val newPidSettings = PidSettings(
                        newStatus.kp,
                        newStatus.ki,
                        newStatus.kd,
                        newStatus.centerAngle,
                        newStatus.safetyLimits
                    )
                    settingsFragmentViewModel.setPidTunningfromRobot(newPidSettings)
                }
            }
        }
    }

    private fun showDevicesToConnect() {
        val bottomSheetDevices =
            supportFragmentManager.findFragmentByTag(BottomSheetDevicesFragment::javaClass.name)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (bottomSheetDevices == null) {
            fragmentTransaction.add(
                BottomSheetDevicesFragment(),
                BottomSheetDevicesFragment::javaClass.name
            )
        }
        fragmentTransaction.commit()
        controlViewModel.setVisibility(false)
    }


    private fun getPermissions() {

        var btPermissions = true

        val requestEnableBt =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    commsRepository.startDiscoverBT()
                } else {
                    Log.d("bluetooth", "El user rechazo el encendido del bluetooth")
                }
            }

        val requestMultiplesPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

                permissions.entries.forEach { permission ->
                    if (!permission.value) {
                        btPermissions = false
                    }
                }

                if (btPermissions) {
                    if (!commsRepository.isBluetoothEnabled()) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        requestEnableBt.launch(enableBtIntent)
                    } else {
                        Log.d("bluetooth", "ya esta encendido")
                        commsRepository.startDiscoverBT()
                    }
                } else {
                    Toast.makeText(this, "ERROR PERMISOS BLUETOOTH", Toast.LENGTH_LONG).show()
                    Log.d("bluetooth", "NO SE OBTUVIERON LOS PERMISOS NECESARIOS")
                }
            }

        requestMultiplesPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        )
    }

//    private fun webViewSetup(){
//
//        val webView = findViewById<WebView>(R.id.webView)
//        webView.webViewClient = WebViewClient()
//
//        webView.apply {
//            loadUrl("https://cdn.flowplayer.com/a30bd6bc-f98b-47bc-abf5-97633d4faea0/hls/de3f6ca7-2db3-4689-8160-0f574a5996ad/playlist.m3u8")
////            loadUrl("http://192.168.1.55:8090/stream/webrtc")
//            settings.javaScriptEnabled = true
//            settings.safeBrowsingEnabled = true
//        }
//
//    }

    fun retryDiscover() {
        commsRepository.startDiscoverBT()
    }

    private fun hideSystemBars() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }


    override fun onDestroy() {
        super.onDestroy()
//        bluetoothManager.destroy()
    }

    companion object {
        const val SKIP_BLUETOOTH = true
    }

}