package ru.iandreyshev.cglab3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.iandreyshev.cglab3.asteroids.presentation.AsteroidsViewModel
import ru.iandreyshev.cglab3.asteroids.presentation.SoundPlayer
import ru.iandreyshev.cglab3.asteroids.ui.AsteroidsScreen
import ru.iandreyshev.cglab3.bezier.domain.BezierModel
import ru.iandreyshev.cglab3.bezier.presentation.BezierViewModel
import ru.iandreyshev.cglab3.bezier.ui.BezierScreen
import ru.iandreyshev.cglab3.common.CGLab3Theme
import ru.iandreyshev.cglab3.guide.GuideScreen
import ru.iandreyshev.cglab3.menu.MenuScreen

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

@Serializable
object Asteroids

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Asteroids
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
            BezierScreen(
                isCanvas = false,
                viewModel = viewModel {
                    BezierViewModel(model = BezierModel())
                }
            )
        }
        composable<Asteroids> {
            val context = LocalContext.current
            AsteroidsScreen(
                viewModel = viewModel {
                    AsteroidsViewModel(soundPlayer = SoundPlayer(context))
                }
            )
        }
    }
}