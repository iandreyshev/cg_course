package ru.iandreyshev.cglabs.navigation

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.iandreyshev.cglab1.bresenhamCircle.BresenhamCircleScreen
import ru.iandreyshev.cglab1.hangman.HangmanScreen
import ru.iandreyshev.cglab1.house.HouseScreen
import ru.iandreyshev.cglab1.initials.InitialsScreen
import ru.iandreyshev.cglab2.data.craft.SoundPlayer
import ru.iandreyshev.cglab2.domain.craft.ElementsStore
import ru.iandreyshev.cglab2.presentation.craft.CraftViewModel
import ru.iandreyshev.cglab2.presentation.craft.ResourcesNameProvider
import ru.iandreyshev.cglab2.presentation.craft.SELECT_ELEMENT_NAV_KEY
import ru.iandreyshev.cglab2.presentation.list.ElementsListViewModel
import ru.iandreyshev.cglab2.ui.craft.CraftScreen
import ru.iandreyshev.cglab2.ui.list.ElementsListScreen
import ru.iandreyshev.cglab2.ui.stories.StoriesScreen
import ru.iandreyshev.cglab2.ui.viewImages.ViewImagesScreen
import ru.iandreyshev.cglab3.asteroids.ui.AsteroidsScreen
import ru.iandreyshev.cglab3.bezier.ui.BezierScreen
import ru.iandreyshev.cglab3.guide.GuideScreen
import ru.iandreyshev.cglab4.cube.ui.FigureScreen
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui.PentagonalIcositetrahedronScreen
import ru.iandreyshev.cglabs.menu.MenuScreen

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val resources = context.resources
    val displayMetrics = resources.displayMetrics

    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = Lab4.PentagonalIcositetrahedron
    ) {
        buildMenuNavigation(navController)
        buildLab1Navigation(displayMetrics, navController)
        buildLab2Navigation(context, resources, displayMetrics, navController)
        buildLab3Navigation(context)
        buildLab4Navigation(context)
    }
}

private fun NavGraphBuilder.buildMenuNavigation(navController: NavHostController) {
    composable<Menu> {
        MenuScreen(navController) {
            lab(1, "Основы создания графических приложений") {
                task("Инициалы", "Рисование и анимирование инициалов на канвасе", Lab1.Initials)
                task("Алгоритм Бресенхэма", "", Lab1.BresenhamCircle)
                task("Дом", "", Lab1.House)
                task("Игра Висилица", "", Lab1.Hangman)
            }
            lab(2, "Программирование двухмерной компьютерной графики") {
                task("Просмотр картинки", "", Lab2.ViewImages)
                task("Игра Алхимия", "", Lab2.AlchemistryCraft)
                task("Редактор историй", "", Lab2.StoryEditor)
            }
            lab(3, "Основы программирования компьютерной графики при помощи OpenGL") {
                task("Треугольник (черновик)", "", Lab3.Guide)
                task("Кривая Безье", "", Lab3.Bezier)
                task("Игра Asteroids", "", Lab3.Asteroids)
            }
            lab(4, "Основы визуализации трехмерных объектов") {
                task("Куб (черновик)", "", Lab4.Cube)
                task("Пентагональный икоситетраэдр", "", Lab4.PentagonalIcositetrahedron)
            }
        }
    }
}

private fun NavGraphBuilder.buildLab1Navigation(
    displayMetrics: DisplayMetrics,
    navController: NavHostController
) {
    composable<Lab1.Initials> {
        InitialsScreen(displaySize = IntSize(displayMetrics.widthPixels, displayMetrics.heightPixels))
    }

    composable<Lab1.House> {
        HouseScreen()
    }

    composable<Lab1.BresenhamCircle> {
        BresenhamCircleScreen()
    }

    composable<Lab1.Hangman> {
        HangmanScreen {
            navController.popBackStack()
        }
    }
}

private fun NavGraphBuilder.buildLab2Navigation(
    context: Context,
    resources: Resources,
    displayMetrics: DisplayMetrics,
    navController: NavHostController
) {
    val elementsStore = ElementsStore()
    elementsStore.initStore()

    composable<Lab2.StoryEditor> {
        StoriesScreen(viewModel = viewModel())
    }
    composable<Lab2.ViewImages> {
        // При повороте экрана чтобы всё работало
        ViewImagesScreen(viewModel = viewModel())
    }
    composable<Lab2.AlchemistryCraft> {
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
            navigateToList = { navController.navigate(Lab2.AlchemistryList) },
            savedStateHandle = it.savedStateHandle
        )
    }
    composable<Lab2.AlchemistryList> {
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

private fun NavGraphBuilder.buildLab3Navigation(context: Context) {
    composable<Lab3.Guide> {
        GuideScreen()
    }
    composable<Lab3.Bezier> {
        BezierScreen()
    }
    composable<Lab3.Asteroids> {
        AsteroidsScreen(context)
    }
}

private fun NavGraphBuilder.buildLab4Navigation(context: Context) {
    composable<Lab4.Cube> {
        FigureScreen()
    }
    composable<Lab4.PentagonalIcositetrahedron> {
        PentagonalIcositetrahedronScreen()
    }
}
