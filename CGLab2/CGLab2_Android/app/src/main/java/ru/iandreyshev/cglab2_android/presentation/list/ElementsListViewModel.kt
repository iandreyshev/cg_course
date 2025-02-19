package ru.iandreyshev.cglab2_android.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.iandreyshev.cglab2_android.domain.ElementsStore

class ElementsListViewModel(
    private val store: ElementsStore
) : ViewModel() {

    val state: State<ElementsListState> by lazy { _state }

    private val _state = mutableStateOf(ElementsListState())

    init {
        initList()
    }

    fun onSelectElement() {
    }

    fun onSearch(query: String) {
    }

    fun onChangeSort() {
    }

    private fun initList() {
        store.elements
            .onEach {
                _state.value = _state.value.copy(
                    all = it.map { ElementsListItem(it.element, it.isEnabled) }
                )
            }
            .launchIn(viewModelScope)
    }

}
