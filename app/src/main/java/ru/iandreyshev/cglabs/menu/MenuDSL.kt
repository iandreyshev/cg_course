package ru.iandreyshev.cglabs.menu

import androidx.navigation.NavController

class MenuBuilder(
    private val navController: NavController
) {
    val items: List<MenuItemState>
        get() = _items

    private val _menuLabBuilder = MenuLabBuilder(navController) {
        _items.add(it)
    }
    private val _items = mutableListOf<MenuItemState>()

    fun lab(number: Int, title: String, builder: MenuLabBuilder.() -> Unit) {
        _items += MenuItemState("$number. ", title, isHeader = true)
        _menuLabBuilder.builder()
    }
}

class MenuLabBuilder(
    private val navController: NavController,
    private val onAddTask: (MenuItemState) -> Unit
) {
    fun task(title: String, navigationRoute: Any) {
        onAddTask(MenuItemState(title) {
            navController.navigate(navigationRoute)
        })
    }
}
