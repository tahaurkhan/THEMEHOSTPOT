package com.example.themehostpot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.themehostpot.data.local.AppDatabase
import com.example.themehostpot.data.repository.LauncherRepositoryImpl
import com.example.themehostpot.ui.screens.HomeScreen
import com.example.themehostpot.ui.theme.THEMEHOSTPOTTheme
import com.example.themehostpot.ui.viewmodel.HomeViewModel
import com.example.themehostpot.ui.viewmodel.HomeViewModelFactory

/**
 * Single Activity Home Launcher Entry Point for MapLauncher.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = LauncherRepositoryImpl(applicationContext, db.hotspotDao())
        HomeViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            THEMEHOSTPOTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}