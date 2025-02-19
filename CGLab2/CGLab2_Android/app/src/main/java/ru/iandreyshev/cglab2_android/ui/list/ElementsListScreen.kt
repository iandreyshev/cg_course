package ru.iandreyshev.cglab2_android.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ru.iandreyshev.cglab2_android.presentation.list.ElementsListItem
import ru.iandreyshev.cglab2_android.presentation.list.ElementsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementsListScreen(
    viewModel: ElementsListViewModel
) {

    val state by viewModel.state

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Elements list")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(state.all) {
                ElementRow(it)
            }
        }
    }
}

@Composable
fun ElementRow(element: ElementsListItem) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
            .alpha(if (element.isEnabled) 1f else 0.32f),
    ) {
        Column {
            Icon(
                Icons.Rounded.Face,
                modifier = Modifier.size(64.dp),
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(element.element.name)
        }
    }
}
