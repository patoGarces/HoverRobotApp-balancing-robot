package com.app.hoverrobot.ui.navigationScreen

sealed class NavigationScreenAction {
    data class OnYawLeftAction(val relativeYaw: Int) : NavigationScreenAction()
    data class OnYawRightAction(val relativeYaw: Int) : NavigationScreenAction()
    data object OnDearmedAction : NavigationScreenAction()
    data class OnFixedDistance(val meters: Float) : NavigationScreenAction()
    data class OnNewDragCompassInteraction(val newDegress: Float) : NavigationScreenAction()
    data class OnNewJoystickInteraction(val x: Int, val y: Int) : NavigationScreenAction()
}