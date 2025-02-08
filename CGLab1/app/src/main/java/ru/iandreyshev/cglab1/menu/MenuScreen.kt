package ru.iandreyshev.cglab1.menu

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.iandreyshev.cglab1.system.CGLab1Theme

@Composable
fun MenuScreen(
    onNavigateToInitials: () -> Unit = {},
    onNavigateToHouse: () -> Unit = {},
    onNavigateToHangman: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onNavigateToInitials) {
            Text("Задание 1: Инициалы")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onNavigateToHouse) {
            Text("Задание 2: Домик в деревне")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            Toast.makeText(context, "Ещё в разработке", Toast.LENGTH_SHORT).show()
        }) {
            Text("Задание 3: Рисование окружности")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onNavigateToHangman) {
            Text("Задание 4: Игра \"Висилица\"")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    CGLab1Theme {
        MenuScreen()
    }
}
