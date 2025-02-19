package ru.iandreyshev.cglab2_android.presentation.craft

import ru.iandreyshev.cglab2_android.domain.Element
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_HEIGHT
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_WIDTH
import java.util.UUID

fun createStartElements(screenWidth: Float, screenHeight: Float): List<CraftElement> {
    val firstWPoint = screenWidth / 3
    val firstHPoint = screenHeight / 5

    val halfWidth = ELEMENT_WIDTH / 2
    val halfHeight = ELEMENT_HEIGHT / 2

    return listOf(
        CraftElement(
            UUID.randomUUID().toString(),
            Element.WATER,
            x = firstWPoint - halfWidth,
            y = firstHPoint * 2 - halfHeight
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.AIR,
            x = firstWPoint * 2 - halfWidth,
            y = firstHPoint * 2 - halfHeight
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.FIRE,
            x = firstWPoint - halfWidth,
            y = firstHPoint * 3 - halfHeight
        ),
        CraftElement(
            UUID.randomUUID().toString(),
            Element.GROUND,
            x = firstWPoint * 2 - halfWidth,
            y = firstHPoint * 3 - halfHeight
        ),
    )
}