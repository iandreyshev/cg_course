package ru.iandreyshev.cglabs.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private const val H_PADDING_DP = 20

@Composable
fun MenuScreen(
    navController: NavController,
    builder: MenuBuilder.() -> Unit,
) {
    val viewModel: MenuViewModel = viewModel {
        val menuLabBuilder = MenuBuilder(navController)
        menuLabBuilder.builder()
        MenuViewModel(menuLabBuilder.items)
    }
    val state by viewModel.state
    val isExpandedMap = remember {
        MutableList(state.labs.size) { false }
            .toMutableStateList()
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        topBar = { TopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            state.labs.forEachIndexed { index, menuLab ->
                MenuRow(menuLab, isExpandedMap[index]) {
                    isExpandedMap[index] = !isExpandedMap[index]
                }
            }
        }
    }
}

private fun LazyListScope.MenuRow(
    lab: MenuLab,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    item {
        Row(
            modifier = Modifier.clickable { onExpand() }
                .padding(horizontal = H_PADDING_DP.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.fillMaxSize()
                    .weight(1f),
                text = "${lab.number}. ${lab.title}",
                fontSize = 19.sp, fontWeight = FontWeight.Medium
            )
            Icon(
                modifier = Modifier.size(32.dp).padding(4.dp),
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }

    if (isExpanded) {
        items(lab.tasks) {
            Row(modifier = Modifier
                .clickable { it.onOpen() }
                .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(start = (2 * H_PADDING_DP).dp, end = H_PADDING_DP.dp)
                        .padding(vertical = 12.dp)
                ) {
                    Text(it.title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text(it.description.ifBlank { "Описание отсутствует" }, fontSize = 15.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = H_PADDING_DP.dp)
            .padding(top = 42.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Компьютерная графика \uD83D\uDCBB",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Лабы выполнил Андрейшев Иван (@iandreyshev)",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
