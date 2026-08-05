package com.shvarsman.coolinar.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.presentation.navigation.AppNavGraph
import com.shvarsman.coolinar.presentation.ui.theme.CoolinarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        enableEdgeToEdge()
        setContent {
            CoolinarTheme {
                val viewModel: MainActivityViewModel = hiltViewModel()
                val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

                if (hasCompletedOnboarding != null) {
                    keepSplashOnScreen = false
                    AppNavGraph(showOnboarding = hasCompletedOnboarding == false)
                }
            }
        }
    }
}