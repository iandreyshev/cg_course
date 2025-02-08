package ru.iandreyshev.cglab1.hangman

data class HangmanState(
    val theme: Theme = Theme.NORMAL,
    val letters: List<Letter> = listOf()
)

enum class Theme {
    NORMAL,
    STRONG;
}

data class Letter(
    val char: Char,
    val state: LetterState,
)

enum class LetterState {
    UNUSED,
    USED_GOOD,
    USED_BAD,
}
