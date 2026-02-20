package ru.iandreyshev.cglab4.mobius

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MobiusScreen(
    viewModel: MobiusViewModel = viewModel { MobiusViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MobiusGLSurfaceView(it)
        },
        update = {
            it.updateState(state)
        }
    )
}