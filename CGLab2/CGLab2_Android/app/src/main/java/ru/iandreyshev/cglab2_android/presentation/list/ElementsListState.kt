package ru.iandreyshev.cglab2_android.presentation.list

import ru.iandreyshev.cglab2_android.domain.Element

data class ElementsListState(
    val all: List<ElementsListItem> = emptyList()
)

data class ElementsListItem(
    val element: Element,
    val isEnabled: Boolean
)
