package ru.iandreyshev.cglabs.menu

import androidx.navigation.NavController

class MenuBuilder(private val navController: NavController) {

    val items: List<MenuLab>
        get() = _items

    private val _items = mutableListOf<MenuLab>()

    fun lab(number: Int, title: String, builder: MenuLabBuilder.() -> Unit) {
        val tasks = mutableListOf<MenuLabTask>()
        val menuLabBuilder = MenuLabBuilder(navController) {
            tasks.add(it)
        }
        menuLabBuilder.builder()
        _items += MenuLab(number, title, tasks)
    }

}

class MenuLabBuilder(
    private val navController: NavController,
    private val onAddTask: (MenuLabTask) -> Unit
) {

    fun task(title: String, description: String = "", route: Any) {
        onAddTask(MenuLabTask(title, description) {
            navController.navigate(route)
        })
    }

}
