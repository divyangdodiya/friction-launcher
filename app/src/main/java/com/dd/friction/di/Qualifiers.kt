package com.dd.friction.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Outlives any screen; for work that must finish even if the user navigates away. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
