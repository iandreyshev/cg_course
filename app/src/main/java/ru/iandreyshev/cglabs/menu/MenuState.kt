package ru.iandreyshev.cglabs.menu

data class MenuState(
    val labs: List<MenuLab>
)

data class MenuLab(
    val number: Int,
    val title: String,
    val tasks: List<MenuLabTask>
)

data class MenuLabTask(
    val title: String,
    val description: String,
    val onOpen: () -> Unit
)
