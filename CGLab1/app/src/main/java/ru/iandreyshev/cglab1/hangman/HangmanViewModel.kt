package ru.iandreyshev.cglab1.hangman

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

    fun onEnterChar(char: Char) {
        val currState = _state.value
        val newCh = char.uppercaseChar()

        if (currState.usedLetters.contains(newCh)) {
            return
        }

        val newLettersMap = currState.letters.toMutableMap()
        newLettersMap[newCh] = Letter(newCh, when {
            currState.roundInfo.word.contains(newCh) -> LetterState.GOOD_USED
            else -> LetterState.BAD_USED
        })

        _state.update {
            it.copy(letters = newLettersMap)
        }
    }

    fun onRestart() {
        initState()
    }

    fun onChangeTheme() {
    }

    private fun initState() {
        _state.update {
            it.copy(
                letters = LETTERS_RANGE.associateWith { Letter(it.uppercaseChar(), LetterState.UNUSED) },
                roundInfo = HangmanData.getRandomInfo()
            )
        }
    }

    companion object {
        private val LETTERS_RANGE = ('А'..'Я')
    }

}
