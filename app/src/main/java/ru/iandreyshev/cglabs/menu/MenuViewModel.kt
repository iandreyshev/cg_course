package ru.iandreyshev.cglabs.menu

import ru.iandreyshev.core.BaseViewModel

class MenuViewModel(
    items: List<MenuLab>
) : BaseViewModel<MenuState, Any>(
    initialState = MenuState(items)
)
