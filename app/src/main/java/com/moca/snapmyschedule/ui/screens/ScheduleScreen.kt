package com.moca.snapmyschedule.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moca.snapmyschedule.R
import com.moca.snapmyschedule.data.model.ClassSession
import com.moca.snapmyschedule.data.model.WeekDay
import com.moca.snapmyschedule.ui.widgets.schedule_screen.DateCarousel
import com.moca.snapmyschedule.ui.widgets.schedule_screen.DayScheduleContent
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    classes: List<ClassSession>,
    onAddClass: () -> Unit,
    onClassClick: (ClassSession) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * La página central representa el día actual.
     * Las páginas anteriores son fechas pasadas
     * y las posteriores son fechas futuras.
     */
    val centerPage = remember {
        Int.MAX_VALUE / 2
    }

    val pagerState = rememberPagerState(
        initialPage = centerPage,
        pageCount = { Int.MAX_VALUE }
    )

    val coroutineScope = rememberCoroutineScope()

    val selectedDate = remember(pagerState.currentPage) {
        getDateForPage(
            page = pagerState.currentPage,
            centerPage = centerPage
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Mi horario")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menu"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onAddClass
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar materia"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            DateCarousel(
                selectedPage = pagerState.currentPage,
                centerPage = centerPage,
                onPageSelected = { page ->

                    if (page != pagerState.currentPage) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                }
            )


            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->

                val date = remember(page) {
                    getDateForPage(
                        page = page,
                        centerPage = centerPage
                    )
                }

                val day = date.toWeekDay()

                val classesForDay = remember(classes, day) {
                    classes
                        .filter { classSession ->
                            classSession.day == day
                        }
                        .sortedBy { classSession ->
                            classSession.startTime
                        }
                }

                DayScheduleContent(
                    date = date,
                    classes = classesForDay,
                    onClassClick = onClassClick
                )
            }
        }
    }
}

private fun getDateForPage(
    page: Int,
    centerPage: Int
): Calendar {
    val dayOffset =
        page.toLong() - centerPage.toLong()

    return Calendar.getInstance().apply {
        // Normaliza la fecha para evitar errores al compararla.
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        add(
            Calendar.DAY_OF_YEAR,
            dayOffset
                .coerceIn(
                    Int.MIN_VALUE.toLong(),
                    Int.MAX_VALUE.toLong()
                )
                .toInt()
        )
    }
}

private fun Calendar.toWeekDay(): WeekDay {
    return when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> WeekDay.MONDAY
        Calendar.TUESDAY -> WeekDay.TUESDAY
        Calendar.WEDNESDAY -> WeekDay.WEDNESDAY
        Calendar.THURSDAY -> WeekDay.THURSDAY
        Calendar.FRIDAY -> WeekDay.FRIDAY
        Calendar.SATURDAY -> WeekDay.SATURDAY
        Calendar.SUNDAY -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }
}