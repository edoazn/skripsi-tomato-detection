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
                id = "1",
                title = "Tomato Disease Prevention Tips",
                content = "Learn how to prevent common tomato diseases with these expert tips.",
                imageUrl = "https://example.com/tomato_disease.jpg",
                date = "2023-10-01",
                author = "John Doe"
            ),
            News(
                id = "2",
                title = "Best Practices for Tomato Cultivation",
                content = "Discover the best practices for cultivating healthy tomato plants.",
                imageUrl = "https://example.com/tomato_cultivation.jpg",
                date = "2023-10-02",
                author = "Jane Smith"
            ),
            News(
                id = "3",
                title = "Tomato Harvesting Techniques",
                content = "Master the techniques for harvesting tomatoes at the right time.",
                imageUrl = "https://example.com/tomato_harvesting.jpg",
                date = "2023-10-03",
                author = "Emily Johnson"
            ),
            News(
                id = "4",
                title = "Tomato Varieties to Try This Season",
                content = "Explore different tomato varieties that thrive in various climates.",
                imageUrl = "https://example.com/tomato_varieties.jpg",
                date = "2023-10-04",
                author = "Michael Brown"
            ),
            News(
                id = "5",
                title = "Organic Tomato Farming Techniques",
                content = "Learn about organic farming techniques for growing tomatoes sustainably.",
                imageUrl = "https://example.com/organic_tomato_farming.jpg",
                date = "2023-10-05",
                author = "Sarah Wilson"
            )
        )
    }
}