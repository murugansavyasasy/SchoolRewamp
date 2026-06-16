package com.vs.schoolmessenger.Testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vs.schoolmessenger.Utils.ShimmerLoadingItem
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp() // Load the Compose UI
        }
    }
}

@Composable
fun MyApp() {
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<List<String>?>(null) }

    // Simulate a network call
    LaunchedEffect(Unit) {
        delay(3000) // Fake network delay
        data = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        isLoading = false
    }

    MyScreen(isLoading = isLoading, data = data)
}

@Composable
fun MyScreen(isLoading: Boolean, data: List<String>?) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            items(5) { ShimmerLoadingItem() } // Show shimmer effect
        } else {
            items(data ?: emptyList()) { item ->
                ListItemView(item)
            }
        }
    }
}

@Composable
fun ListItemView(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Placeholder for Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title Text
        Text(
            text = text,
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.titleMedium
        )

        // Subtitle Text
        Text(
            text = "Subtitle for $text",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // Placeholder for Video
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
        )
    }
}

