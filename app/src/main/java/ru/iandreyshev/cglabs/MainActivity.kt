package ru.iandreyshev.cglabs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.iandreyshev.cglabs.navigation.MainNavHost
import ru.iandreyshev.core.CGLabsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CGLabsTheme {
                MainNavHost()
            }
        }
    }
}
