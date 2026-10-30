package com.dd.friction.ui.model

import com.dd.friction.domain.model.InstalledApp

/** An app as shown on any screen, with whether opening it must go through the gate. */
data class AppItem(val app: InstalledApp, val isDistraction: Boolean)
