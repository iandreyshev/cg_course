package ru.iandreyshev.cglabs.menu

import ru.iandreyshev.core.BaseViewModel

class MenuViewModel(
    items: List<MenuLab>
) : BaseViewModel<MenuState, Any>(
    initialState = MenuState(items)
) {
    fun toggleLab(labIndex: Int) {
        updateState {
            val newExpanded = if (labIndex in expandedLabs) {
                expandedLabs - labIndex
            } else {
                expandedLabs + labIndex
            }
            copy(expandedLabs = newExpanded)
        }
    }

    fun isLabExpanded(labIndex: Int): Boolean = stateValue.expandedLabs.contains(labIndex)
}
