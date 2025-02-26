package ru.iandreyshev.cglab3.ui.guide

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GuideScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            println("CREATE VIEW")
            MyGLSurfaceView(it)
        },
        update = {
            println("UPDATE VIEW")
        }
    )
}
