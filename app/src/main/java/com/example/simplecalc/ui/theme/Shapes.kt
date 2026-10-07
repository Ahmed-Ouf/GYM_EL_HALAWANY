package com.example.simplecalc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp)
)

@Preview(showBackground = true)
@Composable
fun ShapesPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        val shapeList = listOf(
            "Small (8dp)" to Shapes.small,
            "Medium (16dp)" to Shapes.medium,
            "Large (24dp)" to Shapes.large
        )
        shapeList.forEach { (name, shape) ->
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(100.dp, 48.dp)
                    .clip(shape)
                    .background(Color(0xFF0F766E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = name, color = Color.White)
            }
        }
    }
}