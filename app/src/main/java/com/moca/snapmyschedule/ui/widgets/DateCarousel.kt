package com.moca.snapmyschedule.ui.widgets

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    /*
     * Permitimos navegar aproximadamente diez años
     * hacia atrás y diez años hacia delante.
     */
    val startPage = centerPage - DATE_RANGE_DAYS
    val itemCount = DATE_RANGE_DAYS * 2 + 1

    val selectedIndex = (
            selectedPage - startPage
            ).coerceIn(
            minimumValue = 0,
            maximumValue = itemCount - 1
        )

    /*
     * Dejamos aproximadamente dos elementos antes
     * del seleccionado para que quede cerca del centro.
     */
    val initialVisibleIndex = (
            selectedIndex - 2
            ).coerceAtLeast(0)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialVisibleIndex
    )

    val selectedDate = remember(selectedPage) {
        getDateForPage(
            page = selectedPage,
            centerPage = centerPage
        )
    }

    /*
     * Mantiene sincronizado el carrusel cuando el usuario
     * desliza el horario inferior.
     */
    LaunchedEffect(selectedIndex) {
        val targetIndex = (
                selectedIndex - 2
                ).coerceIn(
                minimumValue = 0,
                maximumValue = itemCount - 1
            )

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
                .padding(
                    start = 16.dp,
                    end = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatMonthAndYear(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    onPageSelected(centerPage)
                }
            ) {
                Text("Hoy")
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp),
            contentPadding = PaddingValues(
                horizontal = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            flingBehavior = rememberSnapFlingBehavior(
                lazyListState = listState
            )
        ) {
            items(
                count = itemCount,
                key = { index -> index }
            ) { index ->

                val page = startPage + index

                val date = remember(page) {
                    getDateForPage(
                        page = page,
                        centerPage = centerPage
                    )
                }

                DateCarouselItem(
                    date = date,
                    selected = page == selectedPage,
                    today = isSameDate(
                        first = date,
                        second = Calendar.getInstance()
                    ),
                    onClick = {
                        onPageSelected(page)
                    }
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
        SimpleDateFormat(
            "EEE",
            locale
        ).format(date.time)
            .replace(".", "")
            .replaceFirstChar { character ->
                character.titlecase(locale)
            }
    }

    val monthName = remember(date.timeInMillis, locale) {
        SimpleDateFormat(
            "MMM",
            locale
        ).format(date.time)
            .replace(".", "")
            .lowercase(locale)
    }

    val shape = RoundedCornerShape(18.dp)

    var itemModifier = modifier
        .width(55.dp)
        .height(75.dp)
        .clip(shape)
        .background(
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            shape = shape
        )

    if (today && !selected) {
        itemModifier = itemModifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = shape
        )
    }

    Column(
        modifier = itemModifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = 4.dp,
                vertical = 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )

        Text(
            text = date
                .get(Calendar.DAY_OF_MONTH)
                .toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )

        Text(
            text = if (today) {
                "Hoy"
            } else {
                monthName
            },
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

private fun getDateForPage(
    page: Int,
    centerPage: Int
): Calendar {
    val dayOffset =
        page.toLong() - centerPage.toLong()

    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        add(
            Calendar.DAY_OF_YEAR,
            dayOffset.coerceIn(
                minimumValue = Int.MIN_VALUE.toLong(),
                maximumValue = Int.MAX_VALUE.toLong()
            ).toInt()
        )
    }
}

private fun formatMonthAndYear(
    date: Calendar
): String {
    val locale = Locale.getDefault()

    return SimpleDateFormat(
        "MMMM yyyy",
        locale
    ).format(date.time)
        .replaceFirstChar { character ->
            character.titlecase(locale)
        }
}
