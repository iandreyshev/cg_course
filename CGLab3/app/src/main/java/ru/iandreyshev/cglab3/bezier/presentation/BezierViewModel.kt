package ru.iandreyshev.cglab3.bezier.presentation

import androidx.compose.ui.geometry.Offset
import ru.iandreyshev.cglab3.bezier.domain.BezierModel
import ru.iandreyshev.cglab3.bezier.domain.BezierPoint
import ru.iandreyshev.cglab3.common.BaseViewModel
import ru.iandreyshev.cglab3.common.distanceTo

class BezierViewModel(
    private val model: BezierModel
) : BaseViewModel<BezierState, Any>(
    initialState = BezierState(
        p0 = BezierPoint(position = Offset(144f, 401f)),
        p1 = BezierPoint(position = Offset(222f, 1248f)),
        p2 = BezierPoint(position = Offset(736f, 583f)),
        p3 = BezierPoint(position = Offset(799f, 1574f))
    )
) {

    init {
        updateState {
            copy(curvePoints = model.getPoints(p0, p1, p2, p3))
        }
    }

    private var _pointTouchRadius = 0f
    private var _dragPointId: String? = null
    private var _dragOffset = Offset.Zero

    fun initPointTouchRadius(radius: Float) {
        _pointTouchRadius = radius
    }

    fun onDragStart(pos: Offset) {
        fun tryDrag(point: BezierPoint) {
            if (point.center.distanceTo(pos) > _pointTouchRadius) {
                return
            }

            _dragPointId = point.id
            _dragOffset = point.position - pos
        }

        tryDrag(stateValue.p0)
        tryDrag(stateValue.p1)
        tryDrag(stateValue.p2)
        tryDrag(stateValue.p3)
    }

    fun onDrag(pos: Offset) {
        _dragPointId ?: return

        val newState = with(stateValue) {
            when (_dragPointId) {
                p0.id -> copy(p0 = p0.copy(position = pos + _dragOffset))
                p1.id -> copy(p1 = p1.copy(position = pos + _dragOffset))
                p2.id -> copy(p2 = p2.copy(position = pos + _dragOffset))
                p3.id -> copy(p3 = p3.copy(position = pos + _dragOffset))
                else -> this
            }
        }

        updateState {
            val newPoints = when (val detalization = stateValue.detalization.toInt()) {
                BezierState.MAX_DETALIZATION -> model.getPoints(newState.p0, newState.p1, newState.p2, newState.p3)
                else -> model.getPoints(newState.p0, newState.p1, newState.p2, newState.p3, detalization)
            }
            newState.copy(curvePoints = newPoints)
        }
    }

    fun onDragEnd() {
        _dragPointId = null
    }

    fun onChangeDetalization(newDetalization: Float) {
        updateState {
            val newPoints = when (val detalization = newDetalization.toInt()) {
                BezierState.MAX_DETALIZATION -> model.getPoints(p0, p1, p2, p3)
                else -> model.getPoints(p0, p1, p2, p3, detalization)
            }
            copy(curvePoints = newPoints, detalization = newDetalization)
        }
    }

}
