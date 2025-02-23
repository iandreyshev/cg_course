package ru.iandreyshev.cglab2_android.presentation.craft

sealed interface CraftEvent

data object VibrateTouchBin : CraftEvent
data object SuccessCraft : CraftEvent
