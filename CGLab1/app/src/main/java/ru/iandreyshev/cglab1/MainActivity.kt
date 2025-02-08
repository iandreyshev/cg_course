package ru.iandreyshev.cglab1

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
import ru.iandreyshev.cglab1.hangman.HangmanScreen
import ru.iandreyshev.cglab1.house.HouseScreen
import ru.iandreyshev.cglab1.initials.InitialsScreen
import ru.iandreyshev.cglab1.initials.InitialsViewModel
import ru.iandreyshev.cglab1.menu.MenuScreen
import ru.iandreyshev.cglab1.system.CGLab1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CGLab1Theme {
                MyAppNavHost()
            }
        }
    }
}

@Serializable
object Menu

@Serializable
object Initials

@Serializable
object House

@Serializable
object Hangman

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
                onNavigateToInitials = { navController.navigate(route = Initials) },
                onNavigateToHouse = { navController.navigate(route = House) },
                onNavigateToHangman = { navController.navigate(route = Hangman) }
            )
        }

        composable<Initials> {
            val displayMetrics = LocalContext.current.resources.displayMetrics
            val viewModel = viewModel {
                InitialsViewModel(
                    screenWidth = displayMetrics.widthPixels,
                    screenHeight = displayMetrics.heightPixels
                )
            }

            InitialsScreen(viewModel)
        }

        composable<House> {
            HouseScreen()
        }

        composable<Hangman> {
            HangmanScreen()
        }
    }
}