package com.mocas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.ui.model.BadgeStyle
import com.mocas.ui.model.BottomNavTab

@Composable
fun SnapBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    pendingEventCount: Int = 0,
    badgeStyle: BadgeStyle = BadgeStyle.NUMBER
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 3.dp)
        ) {
            NavigationBar(
                modifier = Modifier.height(80.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "iconScale"
                    )

                    NavigationBarItem(
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

                                            BadgeStyle.NONE -> { /* No mostrar nada */
                                            }
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
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.6f
                            ),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun SnapBottomNavBarPreview() {
    SnapBottomNavBar(
        selectedTab = BottomNavTab.INICIO,
        onTabSelected = {}
    )
}