package com.mocas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mocas.R
import com.mocas.ui.theme.ThemeConfig
import com.mocas.ui.theme.ThemeOption
import com.mocas.ui.viewmodel.ScheduleViewModel

@Composable
fun AppearanceScreen(
    viewModel: ScheduleViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    
    // Interceptar el botón atrás del sistema para cerrar esta pantalla
    BackHandler {
        onBack()
    }

    val isDark = when (settings.themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemDark
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tema_visual), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.seleccionar_paleta_colores),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val screenWidth = maxWidth
                val gridColumns = when {
                    screenWidth >= 840.dp -> 4
                    screenWidth >= 600.dp -> 3
                    else -> 2
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ThemeConfig.themes) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            selected = settings.colorTheme == theme.id,
                            isDark = isDark,
                            onClick = {
                                viewModel.updateSettings(settings.copy(colorTheme = theme.id))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Mode Selection
            var showThemeModeDialog by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                onClick = { showThemeModeDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(settings.themeMode.uppercase()) {
                            "LIGHT" -> Icons.Outlined.LightMode
                            "DARK" -> Icons.Outlined.DarkMode
                            else -> Icons.Outlined.BrightnessAuto
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.modo_apariencia), fontWeight = FontWeight.Bold)
                        Text(
                            text = when(settings.themeMode.uppercase()) {
                                "LIGHT" -> stringResource(R.string.tema_claro)
                                "DARK" -> stringResource(R.string.tema_oscuro)
                                else -> stringResource(R.string.tema_sistema)
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showThemeModeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeModeDialog = false },
                    title = { Text(stringResource(R.string.modo_apariencia), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            listOf("AUTO", "LIGHT", "DARK").forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateSettings(settings.copy(themeMode = mode))
                                            showThemeModeDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.themeMode.uppercase() == mode,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = when(mode) {
                                                "LIGHT" -> stringResource(R.string.tema_claro)
                                                "DARK" -> stringResource(R.string.tema_oscuro)
                                                else -> stringResource(R.string.tema_sistema)
                                            }
                                        )
                                        Text(
                                            text = when(mode) {
                                                "LIGHT" -> stringResource(R.string.tema_desc_claro)
                                                "DARK" -> stringResource(R.string.tema_desc_oscuro)
                                                else -> stringResource(R.string.tema_desc_sistema)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeModeDialog = false }) {
                            Text(stringResource(R.string.cerrar))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: ThemeOption,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isDark) theme.backgroundDark else theme.background
    val primaryColor = if (isDark) theme.primaryDark else theme.primary
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(24.dp),
            color = backgroundColor,
            border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Abc",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp),
                        color = primaryColor.copy(alpha = 0.5f)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(10.dp)
                                .background(primaryColor.copy(alpha = 0.7f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(10.dp)
                                .background(primaryColor.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .background(primaryColor, RoundedCornerShape(8.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(theme.nameRes),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
