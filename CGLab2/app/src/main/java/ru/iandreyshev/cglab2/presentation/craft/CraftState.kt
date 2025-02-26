package ru.iandreyshev.cglab2.presentation.craft

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab2.domain.craft.Element

data class CraftState(
    val elements: List<CraftElement> = emptyList(),
    val dragElement: CraftElement? = null,
    val binCenter: Offset = Offset.Zero,
    val isDragAboveTheBin: Boolean = false,
) {
    val isDrag: Boolean
        get() = dragElement != null
}

data class CraftElement(
    val id: String,
    val element: Element,
    val x: Float,
    val y: Float,
) {

    val pos: Offset
        get() = Offset(x, y)

    fun withPos(newPos: Offset) =
        copy(x = newPos.x, y = newPos.y)

}
