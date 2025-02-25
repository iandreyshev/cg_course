package ru.iandreyshev.cglab2_android.domain.craft

interface IElementNameProvider {
    operator fun get(element: Element): String
}
