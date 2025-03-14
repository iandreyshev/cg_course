package ru.iandreyshev.cglabs.menu

import ru.iandreyshev.core.BaseViewModel

class MenuViewModel(
    items: List<MenuItemState>
) : BaseViewModel<MenuState, Any>(
    initialState = MenuState(items)
)
