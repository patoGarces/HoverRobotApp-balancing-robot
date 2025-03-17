package com.app.hoverrobot

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.hoverrobot.ui.RobotStateViewModel
import com.app.hoverrobot.ui.analisisFragment.AnalisisFragment
import com.app.hoverrobot.ui.navigationScreen.NavigationScreen
import com.app.hoverrobot.ui.settingsScreen.SettingsScreen
import com.app.hoverrobot.ui.statusBarScreen.StatusBarScreen
import com.app.hoverrobot.ui.statusDataScreen.StatusDataScreen

enum class Screens(val route: String){
    STATUS_DATA("Status"),
    NAVIGATION("Navegación"),
    ANALISYS("Análisis"),
    SETTINGS("Configuración"),
    STATUS_BAR("status_bar")
}

@Composable
fun MainScreen(navController: NavHostController) {
    val tabs = listOf(
        Screens.STATUS_DATA,
        Screens.NAVIGATION,
        Screens.ANALISYS,
        Screens.SETTINGS,
    )
    
    val robotStateViewModel: RobotStateViewModel = viewModel()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: Screens.NAVIGATION.route // Default tab is navigation
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (currentRoute != Screens.STATUS_DATA.route) {
            StatusBarScreen(robotStateViewModel)
        }
        NavHost(
            navController = navController,
            startDestination = Screens.NAVIGATION.route,
            modifier = Modifier.fillMaxWidth().weight(1F)
        ) {
            composable(Screens.STATUS_DATA.route) {
                StatusDataScreen(robotStateViewModel)
            }
            composable(Screens.NAVIGATION.route) {
                NavigationScreen(robotStateViewModel)
            }
            composable(Screens.SETTINGS.route) {
                SettingsScreen(robotStateViewModel)
            }
            composable(Screens.ANALISYS.route) {
                AndroidView(
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = View.generateViewId()
                            (context as? AppCompatActivity)?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(this.id, AnalisisFragment())
                                ?.commit()
                        }
                    }
                )
            }
        }

        TabRow(
            modifier = Modifier.widthIn(max = 400.dp).height(35.dp).align(Alignment.CenterHorizontally),
            containerColor = Color.Transparent,
            contentColor = Color.Red,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .height(2.dp)
                        .background(Color.Red)
                )
            },
            divider = {},
            selectedTabIndex = selectedIndex,
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clickable { navController.navigate(tabs[index].route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.route,
                        color = if (index == selectedIndex) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
private fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}