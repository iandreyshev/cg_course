package ru.iandreyshev.cglab1.hangman

object HangmanData {
    private val _info = listOf(
        RoundInfo("Кондуктор", "Ему помог Данила Багров в троллейбусе"),
        RoundInfo("Шаман", "Поёт про Россию, блондин, он такой один"),
        RoundInfo("Айспринг", "Белый офис рядом с Форумом"),
        RoundInfo("Солодков", "Фамилия певца поющего песню булька-булька-барабулька"),
        RoundInfo("Калининград", "Самый западный город РФ"),
    ).map { it.copy(word = it.word.uppercase()) }

    fun getRandomInfo(): RoundInfo = _info.random()
}
