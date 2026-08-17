package com.moca.snapmyschedule.ui.widgets.schedule_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

private const val DATE_RANGE_DAYS = 3650

@Composable
fun DateCarousel(
    selectedPage: Int,
    centerPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val startPage = centerPage - DATE_RANGE_DAYS
    val itemCount = DATE_RANGE_DAYS * 2 + 1

    val selectedIndex = (selectedPage - startPage).coerceIn(0, itemCount - 1)
    val initialVisibleIndex = (selectedIndex - 2).coerceAtLeast(0)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialVisibleIndex
    )

    val selectedDate = remember(selectedPage) {
        getDateForPage(page = selectedPage, centerPage = centerPage)
    }

    LaunchedEffect(selectedIndex) {
        val targetIndex = (selectedIndex - 2).coerceIn(0, itemCount - 1)
        listState.animateScrollToItem(targetIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatMonthAndYear(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FilledTonalButton(
                onClick = { onPageSelected(centerPage) },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Today,
                    contentDescription = null,
                    modifier = Modifier.height(16.dp)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
                Text("Hoy", style = MaterialTheme.typography.labelLarge)
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(count = itemCount, key = { index -> index }) { index ->
                val page = startPage + index

                val date = remember(page) {
                    getDateForPage(page = page, centerPage = centerPage)
                }

                DateCarouselItem(
                    date = date,
                    selected = page == selectedPage,
                    today = isSameDate(first = date, second = Calendar.getInstance()),
                    onClick = { onPageSelected(page) }
                )
            }
        }
    }
}

@Composable
private fun DateCarouselItem(
    date: Calendar,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current.platformLocale

    val dayName = remember(date.timeInMillis, locale) {
        SimpleDateFormat("EEE", locale).format(date.time)
            .replace(".", "")
            .replaceFirstChar { it.titlecase(locale) }
    }

    val monthName = remember(date.timeInMillis, locale) {
        SimpleDateFormat("MMM", locale).format(date.time)
            .replace(".", "")
            .lowercase(locale)
    }

    val isWeekend = remember(date.timeInMillis) {
        val dow = date.get(Calendar.DAY_OF_WEEK)
        dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    val shape = RoundedCornerShape(14.dp)

    // Transiciones suaves entre estados
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = "dateItemBackground"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "dateItemScale"
    )

    var itemModifier = modifier
        .width(46.dp)
        .height(62.dp)
        .scale(scale)
        .clip(shape)

    if (selected) {
        itemModifier = itemModifier.shadow(
            elevation = 4.dp,
            shape = shape,
            clip = false
        )
    }

    itemModifier = itemModifier.background(color = backgroundColor, shape = shape)

    if (today && !selected) {
        itemModifier = itemModifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = shape
        )
    }

    val dayNameColor = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        isWeekend -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = itemModifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = dayNameColor,
            fontWeight = if (isWeekend) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        Text(
            text = date.get(Calendar.DAY_OF_MONTH).toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )

        Text(
            text = if (today) "Hoy" else monthName,
            style = MaterialTheme.typography.labelSmall,
            color = if (today || selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

private fun getDateForPage(page: Int, centerPage: Int): Calendar {
    val dayOffset = page.toLong() - centerPage.toLong()

    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        add(
            Calendar.DAY_OF_YEAR,
            dayOffset.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        )
    }
}

private fun formatMonthAndYear(date: Calendar): String {
    val locale = Locale.getDefault()

    return SimpleDateFormat("MMMM yyyy", locale).format(date.time)
        .replaceFirstChar { it.titlecase(locale) }
}