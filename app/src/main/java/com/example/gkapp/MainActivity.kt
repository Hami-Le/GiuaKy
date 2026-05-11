package com.example.gkapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gkapp.ui.theme.GkAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GkAppTheme {
                // Đã sửa: Chỉ gọi SurfaceLight trực tiếp (nó sẽ tự nhận file AppColors.kt của bạn)
                Surface(modifier = Modifier.fillMaxSize(), color = SurfaceLight) {
                    var currentScreen by remember { mutableStateOf("login") }
                    var currentUserId by remember { mutableStateOf("") }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "login" -> LoginScreen(
                                onLoginSuccess = { role, id ->
                                    currentUserId = id
                                    if (role == "admin") currentScreen = "admin"
                                    else currentScreen = "user"
                                },
                                onNavigateToRegister = { currentScreen = "register" }
                            )
                            "register" -> RegisterScreen(
                                onRegisterSuccess = { currentScreen = "login" },
                                onNavigateBack = { currentScreen = "login" }
                            )
                            "admin" -> AdminScreen(onLogout = { currentScreen = "login" })
                            "user" -> UserScreen(
                                userId = currentUserId,
                                onLogout = { currentScreen = "login"; currentUserId = "" }
                            )
                        }
                    }
                }
            }
        }
    }
}