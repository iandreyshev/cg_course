package ru.iandreyshev.cglab1.initials

data class InitialsState(
    val letter1: InitialsLetter = InitialsLetter(),
    val letter2: InitialsLetter = InitialsLetter(),
    val letter3: InitialsLetter = InitialsLetter()
)
