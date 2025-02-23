package ru.iandreyshev.cglab2_android.presentation.craft

import androidx.compose.ui.geometry.Size
import ru.iandreyshev.cglab2_android.domain.craft.Element
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIDE
import java.util.UUID

fun createStartElements(screenSize: Size): List<CraftElement> {
    val firstWPoint = screenSize.width / 3
    val firstHPoint = screenSize.height / 5

    return listOf(
        CraftElement(
            UUID.randomUUID().toString(),
            Element.WATER,
            x = firstWPoint - ELEMENT_SIDE / 2,
            y = firstHPoint * 2 - ELEMENT_SIDE / 2
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.AIR,
            x = firstWPoint * 2 - ELEMENT_SIDE / 2,
            y = firstHPoint * 2 - ELEMENT_SIDE / 2
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.FIRE,
            x = firstWPoint - ELEMENT_SIDE / 2,
            y = firstHPoint * 3 - ELEMENT_SIDE / 2
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.GROUND,
            x = firstWPoint * 2 - ELEMENT_SIDE / 2,
            y = firstHPoint * 3 - ELEMENT_SIDE / 2
        ),
    )
}