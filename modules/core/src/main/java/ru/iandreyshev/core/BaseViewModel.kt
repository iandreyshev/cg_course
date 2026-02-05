package ru.iandreyshev.core

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<TState, TEvent>(
    initialState: TState
) : ViewModel() {

    val state: State<TState> by lazy { _state }
    val events: Flow<TEvent> by lazy { _event.receiveAsFlow() }

    private val _state = mutableStateOf(initialState)
    private val _event = Channel<TEvent>(Channel.BUFFERED)

    protected val stateValue: TState
        get() = _state.value

    protected fun updateState(modifier: TState.() -> TState) {
        _state.value = _state.value.modifier()
    }

    protected fun emitEvent(event: TEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

}

