package com.example.hoverrobot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hoverrobot.Models.comms.Battery
import com.example.hoverrobot.data.models.comms.MainBoardResponse
import com.example.hoverrobot.data.models.comms.PidSettings
import com.example.hoverrobot.data.models.BluetoothInterface
import com.example.hoverrobot.bluetooth.BluetoothManager
import com.example.hoverrobot.bluetooth.StatusBtEnable
import com.example.hoverrobot.data.utils.StatusEnumBT
import com.example.hoverrobot.data.utils.StatusEnumRobot
import com.example.hoverrobot.databinding.ActivityMainBinding
import com.example.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.example.hoverrobot.ui.analisisFragment.AnalisisViewModel
import com.example.hoverrobot.ui.bottomSheetDevicesBT.BottomSheetDevicesFragment
import com.example.hoverrobot.ui.bottomSheetDevicesBT.BottomSheetDevicesViewModel
import com.example.hoverrobot.ui.bottomSheetDevicesBT.StatusViewBt
import com.example.hoverrobot.ui.controlFragment.ControlFragment
import com.example.hoverrobot.ui.controlFragment.ControlViewModel
import com.example.hoverrobot.ui.settingsFragment.SettingsFragment
import com.example.hoverrobot.ui.settingsFragment.SettingsFragmentViewModel
import com.example.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.example.hoverrobot.ui.statusDataFragment.StatusDataViewModel
import com.google.android.material.tabs.TabLayoutMediator
import java.nio.ByteBuffer

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity(), BluetoothInterface {

    private lateinit var binding: ActivityMainBinding

    private val statusBarViewModel: StatusBarViewModel by viewModels()
    private val statusDataViewModel: StatusDataViewModel by viewModels()
    private val controlViewModel: ControlViewModel by viewModels()
    private val analisisViewModel: AnalisisViewModel by viewModels()
    private val settingsFragmentViewModel: SettingsFragmentViewModel by viewModels()
    private val bottomSheetDevicesViewModel: BottomSheetDevicesViewModel by viewModels()

    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothManager = BluetoothManager(this, this)
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
                if (getStatusBT() == StatusEnumBT.STATUS_CONNECTED) {
                    bluetoothManager.sendPidParam(it)
                } else {
                    Log.d("activity", "No se puede enviar configuración")
                    Toast.makeText(this, "Debe conectarse al bluetooth!", Toast.LENGTH_LONG).show()
                }
            }
        }

        bottomSheetDevicesViewModel.deviceSelected.observe(this) {
            it?.let {
                bluetoothManager.connectDevice(it)
            }
        }

        statusBarViewModel.connectionStatus.observe(this) {

            Log.d("activity", "connetionStatus changed: $it")
            it?.let {
                when (it) {
                    StatusEnumBT.STATUS_INIT,
                    StatusEnumBT.STATUS_DISCONNECT -> {
                        showDevicesToConnect()
                    }

                    StatusEnumBT.STATUS_CONNECTED -> {
//                        binding.btnTest.isEnabled = true
                        controlViewModel.setVisibility(true)
                    }

                    StatusEnumBT.STATUS_ERROR -> {
                        Log.d("connectionStatus", "STATUS_ERROR")
                    }

                    StatusEnumBT.STATUS_DISCOVERING,
                    StatusEnumBT.STATUS_CONNECTING -> {} // TODO()
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
                bluetoothManager.sendJoystickUpdate(it)
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
                    bluetoothManager.startDiscoverBT()
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

                    if (bluetoothManager.isBluetoothEnabled() == StatusBtEnable.BLUETOOTH_OFF) {

                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        requestEnableBt.launch(enableBtIntent)
                    } else if (bluetoothManager.isBluetoothEnabled() == StatusBtEnable.BLUETOOTH_ON) {


                        Log.d("bluetooth", "ya esta encendido")
                        bluetoothManager.startDiscoverBT()
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

    override fun newMessageReceive(buffer: ByteBuffer) {

        val newMainBoardResponse = MainBoardResponse(
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short,
            buffer.float,
            buffer.float,
            buffer.float,

            buffer.float,
            buffer.float,
            buffer.float,
            buffer.float,
            buffer.float,


//            buffer.float,
//            buffer.short,
//            buffer.short,
//            buffer.short,
//            buffer.short,
            buffer.short,
            buffer.short,
            buffer.short
        )

        if (newMainBoardResponse.header == BluetoothManager.HEADER_PACKET.toShort()) {

            statusBarViewModel.setTempImu((newMainBoardResponse.tempUcMain.toFloat() / 10))

            val battery = Battery(
                newMainBoardResponse.batPercent.toInt(),
                newMainBoardResponse.batVoltage.toFloat(),
                newMainBoardResponse.batTemp.toFloat() / 10
            )
            statusBarViewModel.setBatteryStatus(battery)

            StatusEnumRobot.values().getOrNull(newMainBoardResponse.statusCode.toInt())
                ?.let { statusBarViewModel.setStatusRobot(it) }

            statusDataViewModel.setEscsTemp(newMainBoardResponse.tempUcControl.toFloat() / 10)

            statusDataViewModel.setImuTemp(newMainBoardResponse.tempUcMain.toFloat() / 10)

            statusDataViewModel.setGralStatus(newMainBoardResponse.statusCode)

            analisisViewModel.addNewPointData(newMainBoardResponse)

            val newPidSettings = PidSettings(
                newMainBoardResponse.kp,
                newMainBoardResponse.ki,
                newMainBoardResponse.kd,
                newMainBoardResponse.centerAngle,
                newMainBoardResponse.safetyLimits
            )

            settingsFragmentViewModel.setPidTunningfromRobot(newPidSettings)
        }
    }

    override fun setStatusBT(status: StatusEnumBT) {
        statusBarViewModel.setConnectionStatus(status)
    }

    override fun getStatusBT(): StatusEnumBT {
        return statusBarViewModel.connectionStatus.value!!
    }

    override fun getDevicesBT(devices: List<BluetoothDevice>) {
        bottomSheetDevicesViewModel.updateDevicesList(devices)
    }

    override fun initDiscover() {
        bottomSheetDevicesViewModel.updateStatusBtnRefresh(StatusViewBt.BT_SEARCHING)
    }

    override fun stopDiscover() {
        bottomSheetDevicesViewModel.updateStatusBtnRefresh(StatusViewBt.BT_DISCONNECTED)
    }

    fun retryDiscover() {
        bluetoothManager.startDiscoverBT()
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