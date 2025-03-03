package ru.iandreyshev.cglab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.iandreyshev.cglab3.system.CGLab3Theme
import ru.iandreyshev.cglab3.ui.bezier.BezierScreen
import ru.iandreyshev.cglab3.ui.guide.GuideScreen
import ru.iandreyshev.cglab3.ui.menu.MenuScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CGLab3Theme {
                MyAppNavHost()
            }
        }
    }
}

@Serializable
object Menu

@Serializable
object Guide

@Serializable
object Bezier

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                onNavigateToGuide = { navController.navigate(Guide) },
                onNavigateToBezier = { navController.navigate(Bezier) }
            )
        }
        composable<Guide> {
            GuideScreen()
        }
        composable<Bezier> {
            BezierScreen()
        }
    }
}