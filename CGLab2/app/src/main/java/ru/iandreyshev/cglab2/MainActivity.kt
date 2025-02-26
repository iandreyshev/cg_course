package ru.iandreyshev.cglab2

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.iandreyshev.cglab2.data.craft.SoundPlayer
import ru.iandreyshev.cglab2.domain.craft.ElementsStore
import ru.iandreyshev.cglab2.presentation.craft.ResourcesNameProvider
import ru.iandreyshev.cglab2.presentation.craft.SELECT_ELEMENT_NAV_KEY
import ru.iandreyshev.cglab2.presentation.craft.CraftViewModel
import ru.iandreyshev.cglab2.presentation.list.ElementsListViewModel
import ru.iandreyshev.cglab2.system.CGLab2Theme
import ru.iandreyshev.cglab2.ui.craft.CraftScreen
import ru.iandreyshev.cglab2.ui.list.ElementsListScreen
import ru.iandreyshev.cglab2.ui.menu.MenuScreen
import ru.iandreyshev.cglab2.ui.stories.StoriesScreen
import ru.iandreyshev.cglab2.ui.viewImages.ViewImagesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            CGLab2Theme {
                MyAppNavHost()
            }
        }
    }
}

@Serializable
object Menu

@Serializable
object ViewImages

@Serializable
object Stories

@Serializable
object Craft

@Serializable
object ElementsList

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val elementsStore = ElementsStore()
    elementsStore.initStore()

    val displayMetrics = LocalContext.current.resources.displayMetrics

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Menu
    ) {
        composable<Menu> {
            MenuScreen(
                onNavigateToViewImages = { navController.navigate(ViewImages) },
                onNavigateToGame = { navController.navigate(Craft) },
                onNavigateToStoryEditor = { navController.navigate(Stories) }
            )
        }
        composable<Stories> {
            // Premultiplied alpha, Straight alpha
            // Рисовать в растр сразу
            StoriesScreen(viewModel = viewModel())
        }
        composable<ViewImages> {
            // При повороте экрана чтобы всё работало
            ViewImagesScreen(viewModel = viewModel())
        }
        composable<Craft> {
            CraftScreen(
                viewModel = viewModel {
                    CraftViewModel(
                        store = elementsStore,
                        screenSize = Size(
                            displayMetrics.widthPixels.toFloat(),
                            displayMetrics.heightPixels.toFloat()
                        ),
                        soundPlayer = SoundPlayer(context),
                    )
                },
                navigateToList = { navController.navigate(ElementsList) },
                savedStateHandle = it.savedStateHandle
            )
        }

        composable<ElementsList> {
            val resources = LocalContext.current.resources
            val nameProvider = ResourcesNameProvider(resources)

            ElementsListScreen(
                viewModel = viewModel {
                    ElementsListViewModel(
                        store = elementsStore,
                        nameProvider = nameProvider,
                        onSelect = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(SELECT_ELEMENT_NAV_KEY, it)
                            navController.popBackStack()
                        }
                    )
                },
                nameProvider = nameProvider
            )
        }
    }
}