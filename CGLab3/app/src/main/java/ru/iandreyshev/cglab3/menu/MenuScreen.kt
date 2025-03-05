package ru.iandreyshev.cglab3.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onNavigateToGuide: () -> Unit = {},
    onNavigateToBezier: () -> Unit = {},
    onNavigateToStoryEditor: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onNavigateToGuide) {
            Text("Песочница")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onNavigateToBezier) {
            Text("Задание 1: Кривая Безье")
        }
//        Spacer(modifier = Modifier.height(4.dp))
//        Button(onNavigateToStoryEditor) {
//            Text("Задание 3: Редактор сторис")
//        }
    }
}
