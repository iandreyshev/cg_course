package ru.iandreyshev.cglab2_android.presentation.common

import ru.iandreyshev.cglab2_android.R
import ru.iandreyshev.cglab2_android.domain.Element

class ElementDrawableResProvider {

    operator fun get(element: Element): Int? = when (element) {
        Element.FIRE -> R.drawable.ic_element_fire
        else -> null
    }

}
