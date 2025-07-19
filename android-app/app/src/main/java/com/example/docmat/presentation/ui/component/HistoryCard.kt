package com.example.docmat.presentation.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.docmat.R
import com.example.docmat.domain.model.History

@Composable
fun HistoryCard(
    history: History,
    modifier: Modifier = Modifier,
    onClick: (History) -> Unit
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick(history) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Image Section
            Card(
                modifier = Modifier.size(88.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.tomato),
                        contentDescription = "Scan Result Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                // Timestamp
                Text(
                    text = history.timestamp,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Title
                Text(
                    text = history.scanResult,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description (just for detail screen)
//                history.description?.let {
//                    Text(
//                        text = it,
//                        style = MaterialTheme.typography.bodyMedium,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
            }
        }
    }
}






@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HistoryCardPreview() {
    HistoryCard(
        history = History(
            id = "1",
            timestamp = "2023-10-01 12:00",
            scanResult = "Tomato",
            description = "Fresh and organic tomatoes from the local market.",
            imageUrl = null
        ),
        onClick = {}
    )
}