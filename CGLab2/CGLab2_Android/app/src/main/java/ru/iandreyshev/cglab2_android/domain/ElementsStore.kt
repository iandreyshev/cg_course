package ru.iandreyshev.cglab2_android.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ElementsStore {
    val elements: StateFlow<List<StoreElement>> by lazy { _elements }

    private val _elements = MutableStateFlow(listOf<StoreElement>())

    fun initStore() {
        _elements.value = Element.entries
            .map { StoreElement(element = it, it in DEFAULT_ELEMENTS) }
    }

    fun tryCombine(first: Element, second: Element): List<Element> {
        println("Try combine: $first + $second")
        val result = ELEMENTS_MAP[first + second].orEmpty()

        println("Result is:")
        result.forEach {
            println(it)
        }

        _elements.value = _elements.value.map { storeElement ->
            when {
                !storeElement.isEnabled && result.contains(storeElement.element) ->
                    storeElement.copy(isEnabled = true)
                else -> storeElement
            }
        }

        return result
    }
}
