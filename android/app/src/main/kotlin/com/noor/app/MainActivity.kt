package com.noor.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.noor.app.adhkar.AdhkarReminderWorker
import com.noor.app.ui.NoorApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var navigation by mutableStateOf(AppLaunchNavigation.initial(openAdhkar = false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation = AppLaunchNavigation.initial(openAdhkar = intent.shouldOpenAdhkar())
        enableEdgeToEdge()
        setContent {
            NoorApp(
                startDestination = navigation.startDestination,
                openAdhkarRequestId = navigation.adhkarRequestId,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.shouldOpenAdhkar()) {
            navigation = navigation.requestAdhkar()
        }
    }

    private fun Intent?.shouldOpenAdhkar(): Boolean =
        this?.getBooleanExtra(AdhkarReminderWorker.EXTRA_OPEN_ADHKAR, false) == true
}
