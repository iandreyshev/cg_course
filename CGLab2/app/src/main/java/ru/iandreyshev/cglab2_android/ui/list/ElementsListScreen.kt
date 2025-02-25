package ru.iandreyshev.cglab2_android.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.iandreyshev.cglab2_android.domain.craft.Element
import ru.iandreyshev.cglab2_android.domain.craft.IElementNameProvider
import ru.iandreyshev.cglab2_android.presentation.craft.ElementDrawableResProvider
import ru.iandreyshev.cglab2_android.presentation.list.ElementsListItem
import ru.iandreyshev.cglab2_android.presentation.list.ElementsListViewModel
import ru.iandreyshev.cglab2_android.system.ThemeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementsListScreen(
    viewModel: ElementsListViewModel,
    nameProvider: IElementNameProvider,
    imageProvider: ElementDrawableResProvider = ElementDrawableResProvider()
) {
    val state by viewModel.state

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBlue),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = ThemeBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = { Text("Elements list") },
                actions = {
                    IconButton(onClick = viewModel::onChangeSort) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(state.all) {
                ElementRow(it, nameProvider, imageProvider, state.isNavigationEnabled, viewModel::onSelectElement)
            }
        }
    }
}

@Composable
fun ElementRow(
    element: ElementsListItem,
    nameProvider: IElementNameProvider,
    imageProvider: ElementDrawableResProvider,
    isNavigationEnabled: Boolean,
    onSelect: (Element) -> Unit
) {
    Box(
        modifier = Modifier
            .alpha(if (element.isEnabled) 1f else 0.32f)
            .clickable(element.isEnabled && isNavigationEnabled) { onSelect(element.element) },
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val imageBitmap = ImageBitmap.imageResource(imageProvider[element.element])

                when {
                    element.isEnabled -> Image(
                        bitmap = imageBitmap,
                        contentDescription = "",
                        modifier = Modifier.size(60.dp)
                    )

                    else -> Icon(
                        bitmap = imageBitmap,
                        contentDescription = "",
                        modifier = Modifier.size(60.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    nameProvider[element.element],
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
