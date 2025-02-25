package ru.iandreyshev.cglab2_android.presentation.list

import ru.iandreyshev.cglab2_android.domain.craft.Element

data class ElementsListState(
    val all: List<ElementsListItem> = emptyList(),
    val sort: SortType = SortType.ORDINAL,
    val isNavigationEnabled: Boolean = true
)

data class ElementsListItem(
    val element: Element,
    val isEnabled: Boolean
)

enum class SortType {
    ORDINAL,
    BY_NAME_ASC,
    BY_NAME_DESC;
}
