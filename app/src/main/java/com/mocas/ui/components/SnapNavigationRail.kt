package com.mocas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.ui.model.BadgeStyle
import com.mocas.ui.model.BottomNavTab

/**
 * Rail de navegación de altura completa, sin forma de cápsula flotante:
 * ocupa todo el alto del contenedor y su color es el mismo que el fondo
 * de la pantalla, así se integra en vez de verse como una pieza aparte
 * con huecos negros alrededor.
 */
@Composable
fun SnapNavigationRail(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    pendingEventCount: Int = 0,
    badgeStyle: BadgeStyle = BadgeStyle.NUMBER
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        NavigationRail(
            modifier = Modifier.padding(vertical = 8.dp),
            containerColor = Color.Transparent,
            header = {
                // Puedes añadir un logo aquí si quieres
            }
        ) {
            BottomNavTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 380f),
                    label = "iconScale"
                )

                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (tab == BottomNavTab.EVENTOS && pendingEventCount > 0) {
                                    when (badgeStyle) {
                                        BadgeStyle.DOT -> {
                                            Badge(containerColor = MaterialTheme.colorScheme.error)
                                        }

                                        BadgeStyle.NUMBER -> {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = Color.White
                                            ) {
                                                Text(pendingEventCount.toString())
                                            }
                                        }

                                        BadgeStyle.NONE -> {}
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = stringResource(tab.titleRes),
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(tab.titleRes),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    // Sin indicador de fondo (ni pill ni transparente-cortado): solo
                    // el color de icono/texto cambia, así la transición se siente
                    // como parte del mismo panel en vez de un recorte encima.
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .padding(vertical = 14.dp)
                        .testTag("nav_rail_${tab.name.lowercase()}")
                )
            }
        }
    }
}