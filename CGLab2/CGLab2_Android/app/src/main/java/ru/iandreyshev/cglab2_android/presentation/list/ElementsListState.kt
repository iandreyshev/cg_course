package ru.iandreyshev.cglab2_android.presentation.list

import ru.iandreyshev.cglab2_android.domain.Element

data class ElementsListState(
    val all: List<ElementsListItem> = emptyList(),
    val sort: SortType = SortType.ORDINAL
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
