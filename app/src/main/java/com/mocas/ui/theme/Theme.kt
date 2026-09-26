package com.mocas.ui.theme

import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = Outline,
    outlineVariant = OutlineVariant,
    // Dark Charcoal & Grey surface containers (no pure OLED pitch black)
    surfaceContainerLowest = Color(0xFF121318),
    surfaceContainerLow = Color(0xFF171922),
    surfaceContainer = Color(0xFF1E202B),
    surfaceContainerHigh = Color(0xFF252734),
    surfaceContainerHighest = Color(0xFF2C2F3D)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val TotoroDarkColorScheme = darkColorScheme(
    primary = TotoroPrimaryDark,
    onPrimary = TotoroOnPrimaryDark,
    primaryContainer = TotoroPrimaryContainerDark,
    onPrimaryContainer = TotoroOnPrimaryContainerDark,
    secondary = TotoroSecondaryDark,
    onSecondary = TotoroOnSecondaryDark,
    secondaryContainer = TotoroSecondaryContainerDark,
    onSecondaryContainer = TotoroOnSecondaryContainerDark,
    tertiary = TotoroTertiaryDark,
    onTertiary = TotoroOnTertiaryDark,
    tertiaryContainer = TotoroTertiaryContainerDark,
    onTertiaryContainer = TotoroOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = TotoroBackgroundDark,
    onBackground = TotoroOnBackgroundDark,
    surface = TotoroSurfaceDark,
    onSurface = TotoroOnSurfaceDark,
    surfaceVariant = TotoroSurfaceVariantDark,
    onSurfaceVariant = TotoroOnSurfaceVariantDark,
    outline = TotoroOutlineDark,
    outlineVariant = TotoroOutlineVariantDark
)

private val TotoroLightColorScheme = lightColorScheme(
    primary = TotoroPrimary,
    onPrimary = TotoroOnPrimary,
    primaryContainer = TotoroPrimaryContainer,
    onPrimaryContainer = TotoroOnPrimaryContainer,
    secondary = TotoroSecondary,
    onSecondary = TotoroOnSecondary,
    secondaryContainer = TotoroSecondaryContainer,
    onSecondaryContainer = TotoroOnSecondaryContainer,
    tertiary = TotoroTertiary,
    onTertiary = TotoroOnTertiary,
    tertiaryContainer = TotoroTertiaryContainer,
    onTertiaryContainer = TotoroOnTertiaryContainer,
    error = TotoroError,
    onError = TotoroOnError,
    errorContainer = TotoroErrorContainer,
    onErrorContainer = TotoroOnErrorContainer,
    background = TotoroBackground,
    onBackground = TotoroOnBackground,
    surface = TotoroSurface,
    onSurface = TotoroOnSurface,
    surfaceVariant = TotoroSurfaceVariant,
    onSurfaceVariant = TotoroOnSurfaceVariant,
    outline = TotoroOutline,
    outlineVariant = TotoroOutlineVariant
)

private val AsukaDarkColorScheme = darkColorScheme(
    primary = AsukaPrimaryDark,
    onPrimary = AsukaOnPrimaryDark,
    primaryContainer = AsukaPrimaryContainerDark,
    onPrimaryContainer = AsukaOnPrimaryContainerDark,
    secondary = AsukaSecondaryDark,
    onSecondary = AsukaOnSecondaryDark,
    secondaryContainer = AsukaSecondaryContainerDark,
    onSecondaryContainer = AsukaOnSecondaryContainerDark,
    tertiary = AsukaTertiaryDark,
    onTertiary = AsukaOnTertiaryDark,
    tertiaryContainer = AsukaTertiaryContainerDark,
    onTertiaryContainer = AsukaOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = AsukaBackgroundDark,
    onBackground = AsukaOnBackgroundDark,
    surface = AsukaSurfaceDark,
    onSurface = AsukaOnSurfaceDark,
    surfaceVariant = AsukaSurfaceVariantDark,
    onSurfaceVariant = AsukaOnSurfaceVariantDark,
    outline = AsukaOutline,
    outlineVariant = AsukaOutlineVariant
)

private val AsukaLightColorScheme = lightColorScheme(
    primary = AsukaPrimary,
    onPrimary = AsukaOnPrimary,
    primaryContainer = AsukaPrimaryContainer,
    onPrimaryContainer = AsukaOnPrimaryContainer,
    secondary = AsukaSecondary,
    onSecondary = AsukaOnSecondary,
    secondaryContainer = AsukaSecondaryContainer,
    onSecondaryContainer = AsukaOnSecondaryContainer,
    tertiary = AsukaTertiary,
    onTertiary = AsukaOnTertiary,
    tertiaryContainer = AsukaTertiaryContainer,
    onTertiaryContainer = AsukaOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = AsukaBackground,
    onBackground = AsukaOnBackground,
    surface = AsukaSurface,
    onSurface = AsukaOnSurface,
    surfaceVariant = AsukaSurfaceVariant,
    onSurfaceVariant = AsukaOnSurfaceVariant,
    outline = AsukaOutline,
    outlineVariant = AsukaOutlineVariant
)

private val ItsukaDarkColorScheme = darkColorScheme(
    primary = ItsukaPrimaryDark,
    onPrimary = ItsukaOnPrimaryDark,
    primaryContainer = ItsukaPrimaryContainerDark,
    onPrimaryContainer = ItsukaOnPrimaryContainerDark,
    secondary = ItsukaSecondaryDark,
    onSecondary = ItsukaOnSecondaryDark,
    secondaryContainer = ItsukaSecondaryContainerDark,
    onSecondaryContainer = ItsukaOnSecondaryContainerDark,
    tertiary = ItsukaTertiaryDark,
    onTertiary = ItsukaOnTertiaryDark,
    tertiaryContainer = ItsukaTertiaryContainerDark,
    onTertiaryContainer = ItsukaOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = ItsukaBackgroundDark,
    onBackground = ItsukaOnBackgroundDark,
    surface = ItsukaSurfaceDark,
    onSurface = ItsukaOnSurfaceDark,
    surfaceVariant = ItsukaSurfaceVariantDark,
    onSurfaceVariant = ItsukaOnSurfaceVariantDark,
    outline = ItsukaOutline,
    outlineVariant = ItsukaOutlineVariant
)

private val ItsukaLightColorScheme = lightColorScheme(
    primary = ItsukaPrimary,
    onPrimary = ItsukaOnPrimary,
    primaryContainer = ItsukaPrimaryContainer,
    onPrimaryContainer = ItsukaOnPrimaryContainer,
    secondary = ItsukaSecondary,
    onSecondary = ItsukaOnSecondary,
    secondaryContainer = ItsukaSecondaryContainer,
    onSecondaryContainer = ItsukaOnSecondaryContainer,
    tertiary = ItsukaTertiary,
    onTertiary = ItsukaOnTertiary,
    tertiaryContainer = ItsukaTertiaryContainer,
    onTertiaryContainer = ItsukaOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = ItsukaBackground,
    onBackground = ItsukaOnBackground,
    surface = ItsukaSurface,
    onSurface = ItsukaOnSurface,
    surfaceVariant = ItsukaSurfaceVariant,
    onSurfaceVariant = ItsukaOnSurfaceVariant,
    outline = ItsukaOutline,
    outlineVariant = ItsukaOutlineVariant
)

private val KanadeDarkColorScheme = darkColorScheme(
    primary = KanadePrimaryDark,
    onPrimary = KanadeOnPrimaryDark,
    primaryContainer = KanadePrimaryContainerDark,
    onPrimaryContainer = KanadeOnPrimaryContainerDark,
    secondary = KanadeSecondaryDark,
    onSecondary = KanadeOnSecondaryDark,
    secondaryContainer = KanadeSecondaryContainerDark,
    onSecondaryContainer = KanadeOnSecondaryContainerDark,
    tertiary = KanadeTertiaryDark,
    onTertiary = KanadeOnTertiaryDark,
    tertiaryContainer = KanadeTertiaryContainerDark,
    onTertiaryContainer = KanadeOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = KanadeBackgroundDark,
    onBackground = KanadeOnBackgroundDark,
    surface = KanadeSurfaceDark,
    onSurface = KanadeOnSurfaceDark,
    surfaceVariant = KanadeSurfaceVariantDark,
    onSurfaceVariant = KanadeOnSurfaceVariantDark,
    outline = KanadeOutline,
    outlineVariant = KanadeOutlineVariant
)

private val KanadeLightColorScheme = lightColorScheme(
    primary = KanadePrimary,
    onPrimary = KanadeOnPrimary,
    primaryContainer = KanadePrimaryContainer,
    onPrimaryContainer = KanadeOnPrimaryContainer,
    secondary = KanadeSecondary,
    onSecondary = KanadeOnSecondary,
    secondaryContainer = KanadeSecondaryContainer,
    onSecondaryContainer = KanadeOnSecondaryContainer,
    tertiary = KanadeTertiary,
    onTertiary = KanadeOnTertiary,
    tertiaryContainer = KanadeTertiaryContainer,
    onTertiaryContainer = KanadeOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = KanadeBackground,
    onBackground = KanadeOnBackground,
    surface = KanadeSurface,
    onSurface = KanadeOnSurface,
    surfaceVariant = KanadeSurfaceVariant,
    onSurfaceVariant = KanadeOnSurfaceVariant,
    outline = KanadeOutline,
    outlineVariant = KanadeOutlineVariant
)

private val MamimiDarkColorScheme = darkColorScheme(
    primary = MamimiPrimaryDark,
    onPrimary = MamimiOnPrimaryDark,
    primaryContainer = MamimiPrimaryContainerDark,
    onPrimaryContainer = MamimiOnPrimaryContainerDark,
    secondary = MamimiSecondaryDark,
    onSecondary = MamimiOnSecondaryDark,
    secondaryContainer = MamimiSecondaryContainerDark,
    onSecondaryContainer = MamimiOnSecondaryContainerDark,
    tertiary = MamimiTertiaryDark,
    onTertiary = MamimiOnTertiaryDark,
    tertiaryContainer = MamimiTertiaryContainerDark,
    onTertiaryContainer = MamimiOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MamimiBackgroundDark,
    onBackground = MamimiOnBackgroundDark,
    surface = MamimiSurfaceDark,
    onSurface = MamimiOnSurfaceDark,
    surfaceVariant = MamimiSurfaceVariantDark,
    onSurfaceVariant = MamimiOnSurfaceVariantDark,
    outline = MamimiOutline,
    outlineVariant = MamimiOutlineVariant
)

private val MamimiLightColorScheme = lightColorScheme(
    primary = MamimiPrimary,
    onPrimary = MamimiOnPrimary,
    primaryContainer = MamimiPrimaryContainer,
    onPrimaryContainer = MamimiOnPrimaryContainer,
    secondary = MamimiSecondary,
    onSecondary = MamimiOnSecondary,
    secondaryContainer = MamimiSecondaryContainer,
    onSecondaryContainer = MamimiOnSecondaryContainer,
    tertiary = MamimiTertiary,
    onTertiary = MamimiOnTertiary,
    tertiaryContainer = MamimiTertiaryContainer,
    onTertiaryContainer = MamimiOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MamimiBackground,
    onBackground = MamimiOnBackground,
    surface = MamimiSurface,
    onSurface = MamimiOnSurface,
    surfaceVariant = MamimiSurfaceVariant,
    onSurfaceVariant = MamimiOnSurfaceVariant,
    outline = MamimiOutline,
    outlineVariant = MamimiOutlineVariant
)

private val MikuDarkColorScheme = darkColorScheme(
    primary = MikuPrimaryDark,
    onPrimary = MikuOnPrimaryDark,
    primaryContainer = MikuPrimaryContainerDark,
    onPrimaryContainer = MikuOnPrimaryContainerDark,
    secondary = MikuSecondaryDark,
    onSecondary = MikuOnSecondaryDark,
    secondaryContainer = MikuSecondaryContainerDark,
    onSecondaryContainer = MikuOnSecondaryContainerDark,
    tertiary = MikuTertiaryDark,
    onTertiary = MikuOnTertiaryDark,
    tertiaryContainer = MikuTertiaryContainerDark,
    onTertiaryContainer = MikuOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MikuBackgroundDark,
    onBackground = MikuOnBackgroundDark,
    surface = MikuSurfaceDark,
    onSurface = MikuOnSurfaceDark,
    surfaceVariant = MikuSurfaceVariantDark,
    onSurfaceVariant = MikuOnSurfaceVariantDark,
    outline = MikuOutline,
    outlineVariant = MikuOutlineVariant
)

private val MikuLightColorScheme = lightColorScheme(
    primary = MikuPrimary,
    onPrimary = MikuOnPrimary,
    primaryContainer = MikuPrimaryContainer,
    onPrimaryContainer = MikuOnPrimaryContainer,
    secondary = MikuSecondary,
    onSecondary = MikuOnSecondary,
    secondaryContainer = MikuSecondaryContainer,
    onSecondaryContainer = MikuOnSecondaryContainer,
    tertiary = MikuTertiary,
    onTertiary = MikuOnTertiary,
    tertiaryContainer = MikuTertiaryContainer,
    onTertiaryContainer = MikuOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MikuBackground,
    onBackground = MikuOnBackground,
    surface = MikuSurface,
    onSurface = MikuOnSurface,
    surfaceVariant = MikuSurfaceVariant,
    onSurfaceVariant = MikuOnSurfaceVariant,
    outline = MikuOutline,
    outlineVariant = MikuOutlineVariant
)

private val MionDarkColorScheme = darkColorScheme(
    primary = MionPrimaryDark,
    onPrimary = MionOnPrimaryDark,
    primaryContainer = MionPrimaryContainerDark,
    onPrimaryContainer = MionOnPrimaryContainerDark,
    secondary = MionSecondaryDark,
    onSecondary = MionOnSecondaryDark,
    secondaryContainer = MionSecondaryContainerDark,
    onSecondaryContainer = MionOnSecondaryContainerDark,
    tertiary = MionTertiaryDark,
    onTertiary = MionOnTertiaryDark,
    tertiaryContainer = MionTertiaryContainerDark,
    onTertiaryContainer = MionOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MionBackgroundDark,
    onBackground = MionBackgroundDark,
    surface = MionSurfaceDark,
    onSurface = MionOnSurfaceDark,
    surfaceVariant = MionSurfaceVariantDark,
    onSurfaceVariant = MionOnSurfaceVariantDark,
    outline = MionOutline,
    outlineVariant = MionOutlineVariant
)

private val MionLightColorScheme = lightColorScheme(
    primary = MionPrimary,
    onPrimary = MionOnPrimary,
    primaryContainer = MionPrimaryContainer,
    onPrimaryContainer = MionOnPrimaryContainer,
    secondary = MionSecondary,
    onSecondary = MionOnSecondary,
    secondaryContainer = MionSecondaryContainer,
    onSecondaryContainer = MionOnSecondaryContainer,
    tertiary = MionTertiary,
    onTertiary = MionOnTertiary,
    tertiaryContainer = MionTertiaryContainer,
    onTertiaryContainer = MionOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = MionBackground,
    onBackground = MionOnBackground,
    surface = MionSurface,
    onSurface = MionOnSurface,
    surfaceVariant = MionSurfaceVariant,
    onSurfaceVariant = MionOnSurfaceVariant,
    outline = MionOutline,
    outlineVariant = MionOutlineVariant
)

private val RikkaDarkColorScheme = darkColorScheme(
    primary = RikkaPrimaryDark,
    onPrimary = RikkaOnPrimaryDark,
    primaryContainer = RikkaPrimaryContainerDark,
    onPrimaryContainer = RikkaOnPrimaryContainerDark,
    secondary = RikkaSecondaryDark,
    onSecondary = RikkaOnSecondaryDark,
    secondaryContainer = RikkaSecondaryContainerDark,
    onSecondaryContainer = RikkaOnSecondaryContainerDark,
    tertiary = RikkaTertiaryDark,
    onTertiary = RikkaOnTertiaryDark,
    tertiaryContainer = RikkaTertiaryContainerDark,
    onTertiaryContainer = RikkaOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = RikkaBackgroundDark,
    onBackground = RikkaOnBackgroundDark,
    surface = RikkaSurfaceDark,
    onSurface = RikkaOnSurfaceDark,
    surfaceVariant = RikkaSurfaceVariantDark,
    onSurfaceVariant = RikkaOnSurfaceVariantDark,
    outline = RikkaOutline,
    outlineVariant = RikkaOutlineVariant
)

private val RikkaLightColorScheme = lightColorScheme(
    primary = RikkaPrimary,
    onPrimary = RikkaOnPrimary,
    primaryContainer = RikkaPrimaryContainer,
    onPrimaryContainer = RikkaOnPrimaryContainer,
    secondary = RikkaSecondary,
    onSecondary = RikkaOnSecondary,
    secondaryContainer = RikkaSecondaryContainer,
    onSecondaryContainer = RikkaOnSecondaryContainer,
    tertiary = RikkaTertiary,
    onTertiary = RikkaOnTertiary,
    tertiaryContainer = RikkaTertiaryContainer,
    onTertiaryContainer = RikkaOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = RikkaBackground,
    onBackground = RikkaOnBackground,
    surface = RikkaSurface,
    onSurface = RikkaOnSurface,
    surfaceVariant = RikkaSurfaceVariant,
    onSurfaceVariant = RikkaOnSurfaceVariant,
    outline = RikkaOutline,
    outlineVariant = RikkaOutlineVariant
)

private val SakuraDarkColorScheme = darkColorScheme(
    primary = SakuraPrimaryDark,
    onPrimary = SakuraOnPrimaryDark,
    primaryContainer = SakuraPrimaryContainerDark,
    onPrimaryContainer = SakuraOnPrimaryContainerDark,
    secondary = SakuraSecondaryDark,
    onSecondary = SakuraOnSecondaryDark,
    secondaryContainer = SakuraSecondaryContainerDark,
    onSecondaryContainer = SakuraOnSecondaryContainerDark,
    tertiary = SakuraTertiaryDark,
    onTertiary = SakuraOnTertiaryDark,
    tertiaryContainer = SakuraTertiaryContainerDark,
    onTertiaryContainer = SakuraOnTertiaryContainerDark,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = SakuraBackgroundDark,
    onBackground = SakuraOnBackgroundDark,
    surface = SakuraSurfaceDark,
    onSurface = SakuraOnSurfaceDark,
    surfaceVariant = SakuraSurfaceVariantDark,
    onSurfaceVariant = SakuraOnSurfaceVariantDark,
    outline = SakuraOutline,
    outlineVariant = SakuraOutlineVariant
)

private val SakuraLightColorScheme = lightColorScheme(
    primary = SakuraPrimary,
    onPrimary = SakuraOnPrimary,
    primaryContainer = SakuraPrimaryContainer,
    onPrimaryContainer = SakuraOnPrimaryContainer,
    secondary = SakuraSecondary,
    onSecondary = SakuraOnSecondary,
    secondaryContainer = SakuraSecondaryContainer,
    onSecondaryContainer = SakuraOnSecondaryContainer,
    tertiary = SakuraTertiary,
    onTertiary = SakuraOnTertiary,
    tertiaryContainer = SakuraTertiaryContainer,
    onTertiaryContainer = SakuraOnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = SakuraBackground,
    onBackground = SakuraOnBackground,
    surface = SakuraSurface,
    onSurface = SakuraOnSurface,
    surfaceVariant = SakuraSurfaceVariant,
    onSurfaceVariant = SakuraOnSurfaceVariant,
    outline = SakuraOutline,
    outlineVariant = SakuraOutlineVariant
)

// Crimson
private val CrimsonLightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    background = CrimsonBackground,
    surface = CrimsonBackground,
    onBackground = Color(0xFF1C1B1B),
    onSurface = Color(0xFF1C1B1B)
)
private val CrimsonDarkColorScheme = darkColorScheme(
    primary = CrimsonPrimaryDark,
    onPrimary = Color(0xFF601410),
    background = CrimsonBackgroundDark,
    surface = CrimsonBackgroundDark,
    onBackground = Color(0xFFE6E1E0),
    onSurface = Color(0xFFE6E1E0)
)

// Coffee
private val CoffeeLightColorScheme = lightColorScheme(
    primary = CoffeePrimary,
    onPrimary = Color.White,
    background = CoffeeBackground,
    surface = CoffeeBackground,
    onBackground = Color(0xFF231B1B),
    onSurface = Color(0xFF231B1B)
)
private val CoffeeDarkColorScheme = darkColorScheme(
    primary = CoffeePrimaryDark,
    onPrimary = Color(0xFF3E2723),
    background = CoffeeBackgroundDark,
    surface = CoffeeBackgroundDark,
    onBackground = Color(0xFFE7E1E0),
    onSurface = Color(0xFFE7E1E0)
)

// Midnight
private val MidnightLightColorScheme = lightColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color.White,
    background = MidnightBackground,
    surface = MidnightBackground,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E)
)
private val MidnightDarkColorScheme = darkColorScheme(
    primary = MidnightPrimaryDark,
    onPrimary = Color(0xFF1A237E),
    background = MidnightBackgroundDark,
    surface = MidnightBackgroundDark,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
)

// Forest
private val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    background = ForestBackground,
    surface = ForestBackground,
    onBackground = Color(0xFF1A1C19),
    onSurface = Color(0xFF1A1C19)
)
private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = Color(0xFF00390A),
    background = ForestBackgroundDark,
    surface = ForestBackgroundDark,
    onBackground = Color(0xFFE1E3DE),
    onSurface = Color(0xFFE1E3DE)
)

@Composable
fun FlowScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: String = "DEFAULT",
    dynamicColor: Boolean = false, // Use our handcrafted student theme for cohesive aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        (colorTheme == "DYNAMIC" || dynamicColor) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorTheme == "TOTORO" -> if (darkTheme) TotoroDarkColorScheme else TotoroLightColorScheme
        colorTheme == "ASUKA" -> if (darkTheme) AsukaDarkColorScheme else AsukaLightColorScheme
        colorTheme == "ITSUKA" -> if (darkTheme) ItsukaDarkColorScheme else ItsukaLightColorScheme
        colorTheme == "KANADE" -> if (darkTheme) KanadeDarkColorScheme else KanadeLightColorScheme
        colorTheme == "MAMIMI" -> if (darkTheme) MamimiDarkColorScheme else MamimiLightColorScheme
        colorTheme == "MIKU" -> if (darkTheme) MikuDarkColorScheme else MikuLightColorScheme
        colorTheme == "MION" -> if (darkTheme) MionDarkColorScheme else MionLightColorScheme
        colorTheme == "RIKKA" -> if (darkTheme) RikkaDarkColorScheme else RikkaLightColorScheme
        colorTheme == "SAKURA" -> if (darkTheme) SakuraDarkColorScheme else SakuraLightColorScheme
        colorTheme == "CRIMSON" -> if (darkTheme) CrimsonDarkColorScheme else CrimsonLightColorScheme
        colorTheme == "COFFEE" -> if (darkTheme) CoffeeDarkColorScheme else CoffeeLightColorScheme
        colorTheme == "MIDNIGHT" -> if (darkTheme) MidnightDarkColorScheme else MidnightLightColorScheme
        colorTheme == "FOREST" -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is ContextWrapper && context !is ComponentActivity) {
                context = context.baseContext
            }
            (context as? ComponentActivity)?.enableEdgeToEdge(
                statusBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                },
                navigationBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(colorScheme.surface.toArgb())
                } else {
                    SystemBarStyle.light(colorScheme.surface.toArgb(), colorScheme.surface.toArgb())
                }
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
