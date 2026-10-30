package com.dd.friction.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object MenuRoute

@Serializable
data class GateRoute(val packageName: String, val activityName: String)

@Serializable
data object SettingsRoute
