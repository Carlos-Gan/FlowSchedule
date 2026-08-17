package com.moca.snapmyschedule.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onScheduleClick: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "SnapMySchedule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Organiza tu horario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        NavigationDrawerItem(
            label = {
                Text("Mi horario")
            },
            selected =
                currentRoute == AppRoute.Schedule.route,
            onClick = onScheduleClick,
            modifier = Modifier.padding(
                horizontal = 12.dp
            )
        )

        NavigationDrawerItem(
            label = {
                Text("Importar horario")
            },
            selected =
                currentRoute ==
                        AppRoute.ImportSchedule.route,
            onClick = onImportClick,
            modifier = Modifier.padding(
                horizontal = 12.dp
            )
        )
    }
}