package com.app.hoverrobot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.hoverrobot.databinding.ActivityMainBinding
import com.app.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.app.hoverrobot.ui.analisisFragment.AnalisisViewModel
import com.app.hoverrobot.ui.navigationFragment.NavigationFragment
import com.app.hoverrobot.ui.navigationFragment.NavigationViewModel
import com.app.hoverrobot.ui.settingsFragment.SettingsFragment
import com.app.hoverrobot.ui.settingsFragment.SettingsFragmentViewModel
import com.app.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.app.hoverrobot.ui.statusDataScreen.StatusDataViewModel
import com.app.hoverrobot.ui.statusDataScreen.StatusDataScreen
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val statusBarViewModel: StatusBarViewModel by viewModels()
    private val statusDataViewModel: StatusDataViewModel by viewModels()
    private val navigationViewModel: NavigationViewModel by viewModels()
    private val analisisViewModel: AnalisisViewModel by viewModels()
    private val settingsFragmentViewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        webViewSetup()
        setupViewPagerAndTabLayout()
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
                0 -> ComposeFragment { StatusDataScreen() }
                1 -> NavigationFragment()
                2 -> AnalisisFragment()
                3 -> SettingsFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }

    class ComposeFragment(
        private val composable: @Composable () -> Unit
    ) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Devolver un ComposeView que inflará el Composable que pasamos
            return ComposeView(requireContext()).apply {
                setContent {
                    composable()
                }
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