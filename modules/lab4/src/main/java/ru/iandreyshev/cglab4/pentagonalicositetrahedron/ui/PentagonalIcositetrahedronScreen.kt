package ru.iandreyshev.cglab4.pentagonalicositetrahedron.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.iandreyshev.cglab4.pentagonalicositetrahedron.presentation.PentagonalIcositetrahedronViewModel

@Composable
fun PentagonalIcositetrahedronScreen(
    viewModel: PentagonalIcositetrahedronViewModel = viewModel { PentagonalIcositetrahedronViewModel() }
) {
    val state by viewModel.state

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {

        },
        update = { view ->
            view.updateState(state)
        }
    )
}