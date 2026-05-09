package com.biprangshu.newsapp.bookmark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biprangshu.newsapp.ArticleCard
import com.biprangshu.newsapp.domain.model.Article
import com.biprangshu.newsapp.ui.theme.Merriweather

@Composable
fun BookMarkScreen(
    modifier: Modifier = Modifier,
    state: BookmarkState,
    event: (BookMarkEvent) -> Unit,
    navigateToDetails: (Article)-> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Bookmarks",
            fontFamily = Merriweather,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = state.articles,
                key = { article -> article.url }
            ) { article ->
                val isSelected = article.url in state.selectedUrls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (true) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                event(BookMarkEvent.ToggleArticleSelection(article.url))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    ArticleCard(
                        modifier = Modifier.weight(1f),
                        article = article,
                        onClick = {
                            // TODO
                        }
                    )
                }
            }
        }
    }

}