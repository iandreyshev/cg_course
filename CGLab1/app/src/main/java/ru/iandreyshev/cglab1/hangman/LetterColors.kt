package ru.iandreyshev.cglab1.hangman

import androidx.compose.ui.graphics.Color
import ru.iandreyshev.cglab1.hangman.LetterState.BAD_USED
import ru.iandreyshev.cglab1.hangman.LetterState.GOOD_USED
import ru.iandreyshev.cglab1.hangman.LetterState.UNUSED
import ru.iandreyshev.cglab1.hangman.Theme.NORMAL
import ru.iandreyshev.cglab1.hangman.Theme.STRONG

data class LetterColors(
    val unused: Color,
    val goodUsed: Color,
    val badUsed: Color
) {

    operator fun get(state: LetterState) = when (state) {
        UNUSED -> unused
        GOOD_USED -> goodUsed
        BAD_USED -> badUsed
    }

    companion object {
        fun forTheme(theme: Theme) = when (theme) {
            NORMAL -> LetterColors(Color.Black, Color.Green, Color.Red)
            STRONG -> LetterColors(Color.White, Color.Green, Color.Red)
        }
    }

}