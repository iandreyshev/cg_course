package ru.iandreyshev.cglab2_android.presentation.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.domain.ElementsStore
import ru.iandreyshev.cglab2_android.domain.IElementNameProvider
import ru.iandreyshev.cglab2_android.presentation.common.BaseViewModel
import ru.iandreyshev.cglab2_android.presentation.list.SortType.*

class ElementsListViewModel(
    private val store: ElementsStore,
    private val nameProvider: IElementNameProvider,
    private val onSelect: (Element) -> Unit
) : BaseViewModel<ElementsListState, Any>(
    initialState = ElementsListState()
) {

    init {
        initList()
    }

    fun onSelectElement(element: Element) {
        onSelect(element)
    }

    fun onChangeSort() {
        val newSort = when (stateValue.sort) {
            ORDINAL -> BY_NAME_ASC
            BY_NAME_ASC -> BY_NAME_DESC
            BY_NAME_DESC -> ORDINAL
        }

        updateState {
            copy(
                all = stateValue.all.sort(newSort),
                sort = newSort
            )
        }
    }

    private fun initList() {
        store.elements
            .onEach { storeElements ->
                updateState {
                    copy(
                        all = storeElements.map { ElementsListItem(it.element, it.isEnabled) }
                            .sort(stateValue.sort)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun List<ElementsListItem>.sort(sortType: SortType): List<ElementsListItem> {
        return when (sortType) {
            ORDINAL -> sortedBy { it.element.ordinal }
            BY_NAME_ASC -> sortedBy { nameProvider[it.element] }
            BY_NAME_DESC -> sortedByDescending { nameProvider[it.element] }
        }
    }

}
