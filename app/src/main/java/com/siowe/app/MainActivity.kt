package com.siowe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clerk.ui.auth.AuthView
import com.siowe.app.ui.SkyBackground
import com.siowe.app.ui.WeatherScreen
import com.siowe.app.ui.glass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                if (BuildConfig.CLERK_PUBLISHABLE_KEY.isBlank()) MissingKeyScreen() else AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val vm: MainViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    when (state) {
        MainUiState.Loading -> SkyBackground(code = 1, isDay = false) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
        // Первый запуск (и любой запуск без сессии): экран входа Clerk.
        MainUiState.SignedOut -> Box(Modifier.fillMaxSize()) { AuthView() }
        MainUiState.SignedIn -> WeatherScreen()
    }
}

@Composable
private fun MissingKeyScreen() {
    SkyBackground(code = 3, isDay = false) {
        Column(
            Modifier.align(Alignment.Center).padding(24.dp).glass().padding(24.dp)
        ) {
            Text("sioWE", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Не задан Clerk Publishable Key.\n\nДобавьте в local.properties строку\nclerk.publishableKey=pk_test_...\nи пересоберите приложение.",
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
