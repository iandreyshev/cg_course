package ru.iandreyshev.cglab2_android

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
import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.domain.ElementsStore
import ru.iandreyshev.cglab2_android.presentation.common.ElementDrawableResProvider
import ru.iandreyshev.cglab2_android.presentation.common.ResourcesNameProvider
import ru.iandreyshev.cglab2_android.presentation.common.SELECT_ELEMENT_NAV_KEY
import ru.iandreyshev.cglab2_android.presentation.craft.CraftViewModel
import ru.iandreyshev.cglab2_android.presentation.list.ElementsListViewModel
import ru.iandreyshev.cglab2_android.system.CGLab2_AndroidTheme
import ru.iandreyshev.cglab2_android.ui.craft.CraftScreen
import ru.iandreyshev.cglab2_android.ui.list.ElementsListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CGLab2_AndroidTheme {
                MyAppNavHost()
            }
        }
    }
}

@Serializable
object Craft

@Serializable
object ElementsList

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val elementsStore = ElementsStore()
    elementsStore.initStore()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Craft
    ) {
        composable<Craft> {
            val displayMetrics = LocalContext.current.resources.displayMetrics

            CraftScreen(
                viewModel = viewModel {
                    CraftViewModel(
                        store = elementsStore,
                        screenWidth = displayMetrics.widthPixels.toFloat(),
                        screenHeight = displayMetrics.heightPixels.toFloat(),
                        onNavigateToElementsList = {
                            navController.navigate(ElementsList)
                        }
                    )
                },
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