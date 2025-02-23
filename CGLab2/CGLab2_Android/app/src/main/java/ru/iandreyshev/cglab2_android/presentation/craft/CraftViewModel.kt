package ru.iandreyshev.cglab2_android.presentation.craft

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import ru.iandreyshev.cglab2_android.data.craft.Sound
import ru.iandreyshev.cglab2_android.data.craft.SoundPlayer
import ru.iandreyshev.cglab2_android.domain.craft.Element
import ru.iandreyshev.cglab2_android.domain.craft.ElementsStore
import ru.iandreyshev.cglab2_android.presentation.common.BIN_RADIUS_PX
import ru.iandreyshev.cglab2_android.presentation.common.BaseViewModel
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_H_PADDING
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_SIZE
import ru.iandreyshev.cglab2_android.presentation.common.COMBINE_AREA_W_PADDING
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIDE
import ru.iandreyshev.cglab2_android.presentation.common.ELEMENT_SIZE
import ru.iandreyshev.cglab2_android.presentation.common.RANDOM_POS_MARGIN
import java.util.UUID
import kotlin.math.sqrt

class CraftViewModel(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val store: ElementsStore,
    private val soundPlayer: SoundPlayer,
    private val onNavigateToElementsList: () -> Unit
) : BaseViewModel<CraftState, CraftEvent>(
    initialState = CraftState()
) {

    private var _dragOffset = Offset.Zero

    init {
        initState()
    }

    fun initScreenMetrics(insets: Int, binBottomMarginPx: Float) {
        val binCenterY = screenHeight - BIN_RADIUS_PX - insets - binBottomMarginPx
        val binCenter = Offset(screenWidth / 2, binCenterY)

        updateState {
            copy(binCenter = binCenter)
        }
    }

    fun onSpawnElement(element: Element) {
        updateState {
            copy(elements = stateValue.elements + newCraftElement(element))
        }
    }

    fun onDragStart(pos: Offset) {
        val mutableElements = stateValue.elements.toMutableList()
        val elementToMove = mutableElements.reversed()
            .firstOrNull {
                val elementRect = Rect(it.pos, ELEMENT_SIZE)
                elementRect.contains(pos)
            }
            ?: return

        _dragOffset = elementToMove.pos - pos

        mutableElements.remove(elementToMove)
        mutableElements.add(elementToMove)

        updateState {
            copy(elements = mutableElements, dragElement = elementToMove)
        }
    }

    fun onDrag(pos: Offset) {
        val dragElement = stateValue.dragElement ?: return
        val newPos = pos + _dragOffset
        val elements = stateValue.elements
            .map { if (it.id == dragElement.id) it.withPos(newPos) else it }

        val dragCenter = Offset(newPos.x + ELEMENT_SIDE / 2, newPos.y + ELEMENT_SIDE / 2)
        val distance = stateValue.binCenter.distanceTo(dragCenter)
        val isDragAboveTheBin = distance <= BIN_RADIUS_PX

        if (isDragAboveTheBin && !stateValue.isDragAboveTheBin) {
            emitEvent(VibrateTouchBin)
        }

        updateState {
            copy(elements = elements, isDragAboveTheBin = isDragAboveTheBin)
        }
    }

    fun onDragEng() {
        _dragOffset = Offset.Zero

        updateState {
            val newElements = when {
                isDragAboveTheBin -> {
                    soundPlayer.play(Sound.BIN_TOSS)
                    elements.filter { it.id != dragElement?.id }
                }

                else -> elements
            }

            copy(
                dragElement = null,
                elements = newElements
            )
        }

        if (runCombining()) {
            soundPlayer.play(listOf(Sound.SUCCESS_CRAFT_1, Sound.SUCCESS_CRAFT_2).random())
        }
    }

    fun onOpenElementsList() {
        onNavigateToElementsList()
    }

    private fun initState() {
        updateState {
            copy(elements = createStartElements(screenWidth, screenHeight))
        }
    }

    private fun runCombining(): Boolean {
        var isSuccess = false
        var intersections = getCombiningIntersections()

        while (intersections.isNotEmpty()) {
            isSuccess = true
            spawnElements(intersections)
            intersections = getCombiningIntersections()
        }

        return isSuccess
    }

    private fun getCombiningIntersections(): List<Intersecion> {
        val used = mutableSetOf<String>()
        val lastIndex = stateValue.elements.lastIndex

        return stateValue.elements
            .mapIndexedNotNull { index, craftElement ->
                val startIndex = index + 1

                if (used.contains(craftElement.id) || startIndex == lastIndex + 1) {
                    return@mapIndexedNotNull null
                }

                val intersected = stateValue.elements
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
        val newElements = stateValue.elements.toMutableList()

        intersections.forEach {
            newElements.remove(it.first)
            newElements.remove(it.second)
            newElements.add(
                newCraftElement(element = it.result, position = it.first.pos)
            )
        }

        updateState {
            copy(elements = newElements)
        }
    }

    private fun newCraftElement(element: Element, position: Offset? = null): CraftElement {
        val newPos = when (position) {
            null -> {
                val maxX = screenWidth.toInt() - RANDOM_POS_MARGIN - ELEMENT_SIDE.toInt()
                val maxY = screenHeight.toInt() - RANDOM_POS_MARGIN  - ELEMENT_SIDE.toInt()
                Offset(
                    x = (RANDOM_POS_MARGIN..maxX).random().toFloat(),
                    y = (RANDOM_POS_MARGIN..maxY).random().toFloat()
                )
            }
            else -> position
        }

        return CraftElement(
            id = UUID.randomUUID().toString(),
            element = element,
            x = newPos.x,
            y = newPos.y
        )
    }
}

private val CraftElement.combineAreaRect
    get() = Rect(
        Offset(pos.x + COMBINE_AREA_W_PADDING, pos.y + COMBINE_AREA_H_PADDING),
        COMBINE_AREA_SIZE
    )

private fun Offset.distanceTo(offset: Offset): Float {
    val dx = offset.x - x
    val dy = offset.y - y
    return sqrt(dx * dx + dy * dy)
}
