package com.example.hoverrobot

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.databinding.ActivityMainBinding
import com.example.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.example.hoverrobot.ui.analisisFragment.AnalisisViewModel
import com.example.hoverrobot.ui.navigationFragment.NavigationFragment
import com.example.hoverrobot.ui.navigationFragment.NavigationViewModel
import com.example.hoverrobot.ui.settingsFragment.SettingsFragment
import com.example.hoverrobot.ui.settingsFragment.SettingsFragmentViewModel
import com.example.hoverrobot.ui.statusBarFragment.StatusBarFragment
import com.example.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.example.hoverrobot.ui.statusDataFragment.StatusDataFragment
import com.example.hoverrobot.ui.statusDataFragment.StatusDataViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val statusBarViewModel: StatusBarViewModel by viewModels()
    private val statusDataViewModel: StatusDataViewModel by viewModels()
    private val navigationViewModel: NavigationViewModel by viewModels()
    private val analisisViewModel: AnalisisViewModel by viewModels()
    private val settingsFragmentViewModel: SettingsFragmentViewModel by viewModels()

    @Inject
    lateinit var commsRepository: CommsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        webViewSetup()
        setupViewPagerAndTabLayout()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.statusBarContainer, StatusBarFragment())
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun setupViewPagerAndTabLayout() {

        binding.viewPager.adapter = ViewPagerAdapter(this)

        val tabTitles = arrayOf(
            getString(R.string.tab_status),
            getString(R.string.tab_navigation),
            getString(R.string.tab_analisis),
            getString(R.string.tab_settings)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Controlar la visibilidad del StatusBarFragment
                binding.statusBarContainer.isVisible = position != 0
            }
        })

        // Fragment default:
        binding.viewPager.currentItem = 1
        binding.viewPager.isUserInputEnabled = false

    }

    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> StatusDataFragment()
                1 -> NavigationFragment()
                2 -> AnalisisFragment()
                3 -> SettingsFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }

    private fun webViewSetup(){

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()

        webView.apply {
            loadUrl("https://cdn.flowplayer.com/a30bd6bc-f98b-47bc-abf5-97633d4faea0/hls/de3f6ca7-2db3-4689-8160-0f574a5996ad/playlist.m3u8")
//            loadUrl("http://192.168.1.55:8090/stream/webrtc")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
        }

    }

    private fun hideSystemBars() {
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}