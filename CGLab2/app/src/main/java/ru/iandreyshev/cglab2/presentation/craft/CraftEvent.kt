package ru.iandreyshev.cglab2.presentation.craft

sealed interface CraftEvent

data object VibrateTouchBin : CraftEvent
data object SuccessCraft : CraftEvent
