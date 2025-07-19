package com.example.docmat.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import com.example.docmat.domain.model.History
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel: ViewModel() {
    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history = _history.asStateFlow()


    init {
        // Temporary mock data
        _history.value = listOf(
            History(
                id = "1",
                scanResult = "Tomato Disease Detected",
                timestamp = "2023-10-01 10:00:00",
                description = "Leaf spot disease detected in tomato plants.",
                imageUrl = "https://example.com/tomato_disease.jpg"
            ),
            History(
                id = "2",
                scanResult = "Healthy Tomato Plants",
                timestamp = "2023-10-02 11:30:00",
                description = "No diseases detected in tomato plants.",
                imageUrl = "https://example.com/healthy_tomato.jpg"
            ),
            History(
                id = "3",
                scanResult = "Tomato Blight Warning",
                timestamp = "2023-10-03 14:15:00",
                description = "Blight detected in tomato plants. Immediate action required.",
                imageUrl = "https://example.com/tomato_blight.jpg"
            ),
            History(
                id = "4",
                scanResult = "Tomato Pest Alert",
                timestamp = "2023-10-04 09:45:00",
                description = "Pest infestation detected in tomato plants.",
                imageUrl = "https://example.com/tomato_pest.jpg"
            ),
            History(
                id = "5",
                scanResult = "Tomato Growth Monitoring",
                timestamp = "2023-10-05 16:20:00",
                description = "Regular monitoring of tomato plant growth.",
                imageUrl = "https://example.com/tomato_growth.jpg"
            )
        )
    }
}