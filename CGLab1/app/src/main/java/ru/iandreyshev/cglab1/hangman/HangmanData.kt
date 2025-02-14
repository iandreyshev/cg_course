package ru.iandreyshev.cglab1.hangman

object HangmanData {
    private val _info = listOf(
        RoundInfo("Кондуктор", "Ему помог Данила Багров в троллейбусе"),
        RoundInfo("Шаман", "Поёт про Россию, блондин"),
        RoundInfo("Айспринг", "Белый офис рядом с Форумом"),
        RoundInfo("Балтийск", "Самый западный город РФ"),
    ).map { it.copy(word = it.word.uppercase()) }

    fun getRandomInfo(): RoundInfo = _info.random()
}
