package com.mocas.ui.theme

import androidx.compose.ui.graphics.Color

// EduFlow Design System - Primary Palette (Focus Indigo)
val Primary = Color(0xFF24389C)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF3F51B5)
val OnPrimaryContainer = Color(0xFFCACFFF)
val InversePrimary = Color(0xFFBAC3FF)

// EduFlow Design System - Secondary Palette (Success Mint)
val Secondary = Color(0xFF2C6956)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFAEEDD5)
val OnSecondaryContainer = Color(0xFF316D5B)

// EduFlow Design System - Tertiary Palette (Urgent Sunset)
val Tertiary = Color(0xFF6F3000)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF944200)
val OnTertiaryContainer = Color(0xFFFFC5A6)

// EduFlow Design System - Neutral Canvas & Surfaces
val Background = Color(0xFFF8F9FA)
val OnBackground = Color(0xFF191C1D)

val Surface = Color(0xFFF8F9FA)
val OnSurface = Color(0xFF191C1D)
val SurfaceVariant = Color(0xFFE1E3E4)
val OnSurfaceVariant = Color(0xFF454652)

val SurfaceDim = Color(0xFFD9DADB)
val SurfaceBright = Color(0xFFF8F9FA)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF3F4F5)
val SurfaceContainer = Color(0xFFEDEEEF)
val SurfaceContainerHigh = Color(0xFFE7E8E9)
val SurfaceContainerHighest = Color(0xFFE1E3E4)

val InverseSurface = Color(0xFF2E3132)
val InverseOnSurface = Color(0xFFF0F1F2)

val Outline = Color(0xFF757684)
val OutlineVariant = Color(0xFFC5C5D4)
val SurfaceTint = Color(0xFF4355B9)

// EduFlow Design System - Error
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// Fixed Variants
val PrimaryFixed = Color(0xFFDEE0FF)
val PrimaryFixedDim = Color(0xFFBAC3FF)
val OnPrimaryFixed = Color(0xFF00105C)
val OnPrimaryFixedVariant = Color(0xFF293CA0)

val SecondaryFixed = Color(0xFFB1EFD8)
val SecondaryFixedDim = Color(0xFF96D3BD)
val OnSecondaryFixed = Color(0xFF002118)
val OnSecondaryFixedVariant = Color(0xFF0D503F)

val TertiaryFixed = Color(0xFFFFDBC9)
val TertiaryFixedDim = Color(0xFFFFB68D)
val OnTertiaryFixed = Color(0xFF331200)
val OnTertiaryFixedVariant = Color(0xFF763300)

// Dark Theme Derived Palette (EduFlow focus)
val PrimaryDark = Color(0xFFBAC3FF)
val OnPrimaryDark = Color(0xFF001970)
val PrimaryContainerDark = Color(0xFF293CA0)
val OnPrimaryContainerDark = Color(0xFFDEE0FF)

val SecondaryDark = Color(0xFF96D3BD)
val OnSecondaryDark = Color(0xFF003828)
val SecondaryContainerDark = Color(0xFF0D503F)
val OnSecondaryContainerDark = Color(0xFFB1EFD8)

val TertiaryDark = Color(0xFFFCB88E)
val OnTertiaryDark = Color(0xFF4C2100)
val TertiaryContainerDark = Color(0xFF6E3200)
val OnTertiaryContainerDark = Color(0xFFFFDBC9)

val BackgroundDark = Color(0xFF191C1D)
val OnBackgroundDark = Color(0xFFE1E3E4)
val SurfaceDark = Color(0xFF191C1D)
val OnSurfaceDark = Color(0xFFE1E3E4)
val SurfaceVariantDark = Color(0xFF454652)
val OnSurfaceVariantDark = Color(0xFFC5C5D4)

// Subject Palette for Timetable & Categorization (Maintained from previous style)
val SubjectColors = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFF8B5CF6), // Violet
    Color(0xFFF59E0B), // Amber
    Color(0xFFEF4444), // Red
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEC4899), // Pink
    Color(0xFF14B8A6), // Teal
    Color(0xFF6366F1), // Indigo
    Color(0xFF84CC16)  // Lime
)

val SubjectColorHexes = listOf(
    "#3B82F6",
    "#10B981",
    "#8B5CF6",
    "#F59E0B",
    "#EF4444",
    "#06B6D4",
    "#EC4899",
    "#14B8A6",
    "#6366F1",
    "#84CC16"
)

// Legacy Aliases for existing components (Bento style names)
val IndigoPrimary = Primary
val IndigoPrimaryVariant = PrimaryContainer
val IndigoLight = PrimaryFixedDim
val IndigoDark = OnPrimaryFixed
val IndigoContainerLight = PrimaryFixed
val IndigoContainerDark = PrimaryContainerDark

val TurquoiseSecondary = Secondary
val TurquoiseLight = SecondaryFixedDim
val TurquoiseDark = OnSecondaryFixed
val TurquoiseContainerLight = SecondaryFixed

val AccentAmber = Tertiary
val AccentAmberContainer = TertiaryFixed
val AccentRose = Error
val AccentRoseContainer = ErrorContainer
val AccentEmerald = Secondary
val AccentEmeraldContainer = SecondaryFixed
val AccentViolet = PrimaryFixed
val AccentVioletContainer = PrimaryFixedDim

val BackgroundLight = Background
val SurfaceLight = SurfaceContainerLowest
val SurfaceVariantLight = SurfaceVariant
val BentoTileLight = SurfaceContainerLowest
val BentoBorderLight = OutlineVariant
val TextPrimaryLight = OnSurface
val TextSecondaryLight = OnSurfaceVariant

val BentoTileDark = SurfaceDark
val BentoBorderDark = Outline
val TextPrimaryDark = OnSurfaceDark
val TextSecondaryDark = OnSurfaceVariantDark
