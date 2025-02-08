package ru.iandreyshev.cglab1.hangman

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HangmanViewModel : ViewModel() {

    private val _state = MutableStateFlow(HangmanState())
    val state = _state.asStateFlow()

    init {
        initState()
    }

    fun onEnterLetter() {
    }

    fun onRestart() {
    }

    fun onChangeTheme() {
    }

    private fun initState() {
    }

}
