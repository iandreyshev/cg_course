package ru.iandreyshev.cglab2.domain.craft

interface IElementNameProvider {
    operator fun get(element: Element): String
}
