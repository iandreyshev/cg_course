package ru.iandreyshev.cglabs.menu

data class MenuState(
    val items: List<MenuItemState>
)

data class MenuItemState(
    val title: String,
    val description: String = "",
    val isHeader: Boolean = false,
    val onOpen: () -> Unit = {}
)
