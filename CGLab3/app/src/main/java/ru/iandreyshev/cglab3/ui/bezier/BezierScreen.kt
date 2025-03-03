package ru.iandreyshev.cglab3.ui.bezier

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ru.iandreyshev.cglab3.ui.guide.GuideGLSurfaceView

@Composable
fun BezierScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            GuideGLSurfaceView(it)
        }
    )
}
