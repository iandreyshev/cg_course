package ru.iandreyshev.cglab1.hangman

const val MAX_BAD_LETTERS = 7

data class HangmanState(
    val gameState: GameState = GameState.PLAYING,
    val theme: Theme = Theme.NORMAL,
    val roundInfo: RoundInfo = RoundInfo(),
    val letters: Map<Char, Letter> = emptyMap(),
    val history: List<Letter> = emptyList()
) {

    val goodUsedLetters = letters.filter { it.value.state == LetterState.GOOD_USED }
    val badUsedLetters = letters.filter { it.value.state == LetterState.BAD_USED }
    val usedLetters = goodUsedLetters + badUsedLetters
    val lettersToWin = roundInfo.wordUniqueLettersCount

}

enum class GameState {
    PLAYING,
    FINISHED_WIN,
    FINISHED_LOSE;
}

enum class Theme {
    NORMAL,
    STRONG;
}

data class Letter(
    val char: Char,
    val state: LetterState,
)

data class RoundInfo(
    val word: String = "",
    val clue: String = ""
) {
    val wordUniqueLettersCount = word.toSet().count()
}

enum class LetterState {
    UNUSED,
    GOOD_USED,
    BAD_USED,
}
