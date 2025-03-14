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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

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

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        topBar = { TopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(state.items) {
                MenuRow(it)
            }
        }
    }
}

@Composable
private fun MenuRow(state: MenuItemState) {
    when {
        state.isHeader -> {
            Text(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 8.dp),
                text = "${state.title} ${state.description}",
                fontSize = 20.sp, fontWeight = FontWeight.Medium
            )
        }

        else -> {
            Row(modifier = Modifier
                .clickable { state.onOpen() }
                .fillMaxSize()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(state.title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text("Описание задания", fontSize = 15.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 42.dp, bottom = 10.dp)
    ) {
        Text(
            text = "Компьютерная графика \uD83D\uDCBB",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Лабы выполнил Андрейшев Иван",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
