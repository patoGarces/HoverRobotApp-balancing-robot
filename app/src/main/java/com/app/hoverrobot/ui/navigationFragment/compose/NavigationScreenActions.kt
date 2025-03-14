package com.app.hoverrobot.ui.navigationFragment.compose

sealed class NavigationScreenAction {
    data class OnYawLeftAction(val relativeYaw: Int) : NavigationScreenAction()
    data class OnYawRightAction(val relativeYaw: Int) : NavigationScreenAction()
    data object OnDearmedAction : NavigationScreenAction()
    data class OnFixedDistance(val meters: Float) : NavigationScreenAction()
    data class OnNewDragCompassInteraction(val newDegress: Float) : NavigationScreenAction()
    data class OnNewJoystickInteraction(val x: Float, val y: Float) : NavigationScreenAction()
}
