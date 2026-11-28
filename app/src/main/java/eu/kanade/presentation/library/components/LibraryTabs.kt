package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.components.mochikuCardBorderStroke
import tachiyomi.domain.category.model.Category

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val rowState = rememberLazyListState()
    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex in categories.indices) {
            rowState.animateScrollToItem(currentPageIndex)
        }
    }
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.zIndex(2f)) {
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(categories) { index, category ->
                val selected = currentPageIndex == index
                val count = getItemCountForCategory(category)
                Surface(
                    onClick = { onTabItemClick(index) },
                    shape = CircleShape,
                    color = if (selected) colorScheme.primary else colorScheme.surfaceContainerLow,
                    border = if (selected) null else mochikuCardBorderStroke(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = category.visualName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                        )
                        if (count != null) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) colorScheme.primary else colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) colorScheme.onPrimary else colorScheme.surfaceContainerHigh,
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
