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

        if (currState.usedLetters.contains(newCh) || !currState.letters.containsKey(newCh)) {
            return
        }

        val newLettersMap = currState.letters.toMutableMap()
        val newLetter = Letter(
            newCh, when {
                currState.roundInfo.word.contains(newCh) -> LetterState.GOOD_USED
                else -> LetterState.BAD_USED
            }
        )
        newLettersMap[newCh] = newLetter

        _state.update {
            it.copy(letters = newLettersMap, history = it.history + listOf(newLetter))
        }

        val newGameState = when {
            _state.value.badUsedLetters.size == MAX_BAD_LETTERS -> GameState.FINISHED_LOSE
            _state.value.lettersToWin == _state.value.goodUsedLetters.size -> GameState.FINISHED_WIN
            else -> GameState.PLAYING
        }

        _state.update {
            it.copy(gameState = newGameState)
        }
    }

    fun onRestart() {
        initState()
    }

    fun onChangeTheme() {
        _state.update {
            it.copy(
                theme = when (_state.value.theme) {
                    Theme.NORMAL -> Theme.STRONG
                    Theme.STRONG -> Theme.NORMAL
                }
            )
        }
    }

    private fun initState() {
        _state.update {
            it.copy(
                letters = LETTERS_RANGE.associateWith { Letter(it.uppercaseChar(), LetterState.UNUSED) },
                roundInfo = HangmanData.getRandomInfo(),
                gameState = GameState.PLAYING,
                history = emptyList()
            )
        }
    }

    companion object {
        private val LETTERS_RANGE = ('А'..'Я')
    }

}
