package ru.iandreyshev.cglab2_android.domain

interface IElementNameProvider {
    operator fun get(element: Element): String
}
