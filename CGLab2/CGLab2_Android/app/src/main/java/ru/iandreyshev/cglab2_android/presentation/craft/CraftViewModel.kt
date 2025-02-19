package ru.iandreyshev.cglab2_android.presentation.craft

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import ru.iandreyshev.cglab2_android.domain.ElementsStore
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_H_PADDING
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_SIZE
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_W_PADDING
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIZE
import java.util.UUID

class CraftViewModel(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val store: ElementsStore,
    private val onNavigateToElementsList: () -> Unit
) : ViewModel() {

    val state: State<CraftState> by lazy { _state }

    private val _state = mutableStateOf(CraftState())
    private var _dragOffset = Offset.Zero

    init {
        initState()
    }

    fun onDragStart(pos: Offset) {
        val mutableElements = _state.value.elements.toMutableList()
        val elementToMove = mutableElements.reversed()
            .firstOrNull {
                val elementRect = Rect(it.pos, ELEMENT_SIZE)
                elementRect.contains(pos)
            }
            ?: return

        _dragOffset = elementToMove.pos - pos

        mutableElements.remove(elementToMove)
        mutableElements.add(elementToMove)

        _state.value = _state.value.copy(
            elements = mutableElements,
            dragElement = elementToMove
        )
    }

    fun onDrag(pos: Offset) {
        val dragElement = _state.value.dragElement ?: return
        val newPos = pos + _dragOffset
        val elements = _state.value.elements
            .map { if (it.id == dragElement.id) it.withPos(newPos) else it }

        _state.value = _state.value.copy(elements = elements)
    }

    fun onDragEng() {
        _dragOffset = Offset.Zero
        _state.value = _state.value.copy(dragElement = null)
        runCombining()
    }

    fun onOpenElementsList() {
        onNavigateToElementsList()
    }

    private fun initState() {
        _state.value = _state.value.copy(
            elements = createStartElements(screenWidth, screenHeight)
        )
    }

    private fun runCombining() {
        var intersections = getCombiningIntersections()

        while (intersections.isNotEmpty()) {
            spawnElements(intersections)
            intersections = getCombiningIntersections()
        }
    }

    private fun getCombiningIntersections(): List<Intersecion> {
        val used = mutableSetOf<String>()
        val lastIndex = _state.value.elements.lastIndex

        return _state.value.elements
            .mapIndexedNotNull { index, craftElement ->
                val startIndex = index + 1

                if (used.contains(craftElement.id) || startIndex == lastIndex + 1) {
                    return@mapIndexedNotNull null
                }

                val intersected = _state.value.elements
                    .subList(startIndex, lastIndex + 1)
                    .firstOrNull {
                        !used.contains(it.id) &&
                                craftElement.combineAreaRect.overlaps(it.combineAreaRect)
                    }
                    ?: return@mapIndexedNotNull null

                used.add(craftElement.id)
                used.add(intersected.id)

                store.tryCombine(craftElement.element, intersected.element)
                    .map { Intersecion(craftElement, intersected, it) }
            }
            .flatten()
    }

    private fun spawnElements(intersections: List<Intersecion>) {
        val newElements = _state.value.elements.toMutableList()

        intersections.forEach {
            newElements.remove(it.first)
            newElements.remove(it.second)
            newElements.add(
                CraftElement(
                    id = UUID.randomUUID().toString(),
                    element = it.result,
                    x = it.first.x,
                    y = it.first.y
                )
            )
        }

        _state.value = _state.value.copy(elements = newElements)
    }
}

val CraftElement.combineAreaRect
    get() = Rect(
        Offset(pos.x + COMBINE_AREA_W_PADDING, pos.y + COMBINE_AREA_H_PADDING),
        COMBINE_AREA_SIZE
    )
