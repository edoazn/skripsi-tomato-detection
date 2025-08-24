package com.example.docmat.presentation.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import com.example.docmat.domain.model.News
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _news = MutableStateFlow<List<News>>(emptyList())
    val news = _news.asStateFlow()

    init {
        // Temporary mock data
        _news.value = listOf(
            News(
                id = 1,
                title = "Tomato Disease Prevention Tips",
                description = "Learn how to prevent common tomato diseases with these expert tips.",
                content = "Learn how to prevent common tomato diseases with these expert tips. Proper plant spacing, adequate watering, and regular monitoring are key factors.",
                imageUrl = "https://example.com/tomato_disease.jpg",
                publishedAt = "2023-10-01T00:00:00Z",
                source = "Agriculture Expert"
            ),
            News(
                id = 2,
                title = "Best Practices for Tomato Cultivation",
                description = "Discover the best practices for cultivating healthy tomato plants.",
                content = "Discover the best practices for cultivating healthy tomato plants. Soil preparation, proper fertilization, and pest management are crucial.",
                imageUrl = "https://example.com/tomato_cultivation.jpg",
                publishedAt = "2023-10-02T00:00:00Z",
                source = "Farm Daily"
            ),
            News(
                id = 3,
                title = "Tomato Harvesting Techniques",
                description = "Master the techniques for harvesting tomatoes at the right time.",
                content = "Master the techniques for harvesting tomatoes at the right time. Timing, proper handling, and storage methods are essential.",
                imageUrl = "https://example.com/tomato_harvesting.jpg",
                publishedAt = "2023-10-03T00:00:00Z",
                source = "Harvest Magazine"
            )
        )
    }
}