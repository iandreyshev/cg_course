package ru.iandreyshev.cglab1.initials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val GRAVITATION_CONSTANT = 1000f
private const val LETTER_1_JUMP_VELOCITY = 2000f
private const val LETTER_2_JUMP_VELOCITY = 1500f
private const val LETTER_3_JUMP_VELOCITY = 1000f

class InitialsViewModel(
    private val screenWidth: Int,
    private val screenHeight: Int
) : ViewModel() {

    private val _state = MutableStateFlow(InitialsState())
    val state = _state.asStateFlow()

    init {
        initState()
        enterGameLoop()
    }

    private fun initState() {
        val letterWidth = (screenWidth - LETTERS_GUTTER - LETTERS_GUTTER - CANVAS_PADDING - CANVAS_PADDING) / 3

        _state.update {
            it.copy(
                letter1 = it.letter1.copy(xPosition = CANVAS_PADDING, width = letterWidth),
                letter2 = it.letter2.copy(xPosition = CANVAS_PADDING + letterWidth + LETTERS_GUTTER, width = letterWidth),
                letter3 = it.letter3.copy(xPosition = CANVAS_PADDING + 2 * letterWidth + 2 * LETTERS_GUTTER, width = letterWidth)
            )
        }
    }

    private fun enterGameLoop() {
        viewModelScope.launch {
            var lastTickTime = System.currentTimeMillis()

            while (true) {
                val currentTickTime = System.currentTimeMillis()
                val elapsedTime = (currentTickTime - lastTickTime) / 1000f
                doTick(elapsedTime)
                delay(MILLIS_IN_SEC / MAX_FPS)
                lastTickTime = currentTickTime
            }
        }
    }

    private fun doTick(elapsedTime: Float) {
        _state.update {
            it.copy(
                letter1 = doLetterTick(elapsedTime, it.letter1, LETTER_1_JUMP_VELOCITY),
                letter2 = doLetterTick(elapsedTime, it.letter2, LETTER_2_JUMP_VELOCITY),
                letter3 = doLetterTick(elapsedTime, it.letter3, LETTER_3_JUMP_VELOCITY)
            )
        }
    }

    private fun doLetterTick(elapsedTime: Float, letter: InitialsLetter, jumpVelocity: Float): InitialsLetter {
        var newVelocity = letter.velocity + GRAVITATION_CONSTANT * elapsedTime
        var newYPosition = letter.yPosition + letter.velocity * elapsedTime

        if (newYPosition + LETTER_HEIGHT > screenHeight) {
            newVelocity = -jumpVelocity
            newYPosition = screenHeight - LETTER_HEIGHT
        }

        return letter.copy(
            velocity = newVelocity,
            yPosition = newYPosition
        )
    }

    companion object {
        private const val MILLIS_IN_SEC = 1000L
        private const val MAX_FPS = 60L
    }

}
