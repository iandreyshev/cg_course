package ru.iandreyshev.cglab2_android.model

class Element(
    val name: String
)

enum class ElementIcon {

}

infix fun Element.combineWith(other: Element): Element? {
}
