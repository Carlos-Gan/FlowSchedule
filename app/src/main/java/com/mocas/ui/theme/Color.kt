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

val BackgroundDark = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFE1E3E4)
val SurfaceDark = Color(0xFF000000)
val OnSurfaceDark = Color(0xFFE1E3E4)
val SurfaceVariantDark = Color(0xFF121415)
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

// Totoro Theme Colors (Light)
val TotoroPrimary = Color(0xFF3C6090)
val TotoroOnPrimary = Color(0xFFFFFFFF)
val TotoroPrimaryContainer = Color(0xFFD4E3FF)
val TotoroOnPrimaryContainer = Color(0xFF224876)
val TotoroSecondary = Color(0xFF545F71)
val TotoroOnSecondary = Color(0xFFFFFFFF)
val TotoroSecondaryContainer = Color(0xFFD8E3F8)
val TotoroOnSecondaryContainer = Color(0xFF3D4758)
val TotoroTertiary = Color(0xFF6E5676)
val TotoroOnTertiary = Color(0xFFFFFFFF)
val TotoroTertiaryContainer = Color(0xFFF7D8FF)
val TotoroOnTertiaryContainer = Color(0xFF553F5D)
val TotoroBackground = Color(0xFFF9F9FF)
val TotoroOnBackground = Color(0xFF191C20)
val TotoroSurface = Color(0xFFF9F9FF)
val TotoroOnSurface = Color(0xFF191C20)
val TotoroSurfaceVariant = Color(0xFFE0E2EC)
val TotoroOnSurfaceVariant = Color(0xFF43474E)
val TotoroOutline = Color(0xFF74777F)
val TotoroOutlineVariant = Color(0xFFC3C6CF)
val TotoroSurfaceContainer = Color(0xFFEDEDF4)

// Totoro Dark (Estimated based on M3 logic)
val TotoroPrimaryDark = Color(0xFFA6C8FF)
val TotoroOnPrimaryDark = Color(0xFF00315F)
val TotoroPrimaryContainerDark = Color(0xFF224876)
val TotoroOnPrimaryContainerDark = Color(0xFFD4E3FF)
val TotoroSecondaryDark = Color(0xFFBCC7DC)
val TotoroOnSecondaryDark = Color(0xFF263141)
val TotoroSecondaryContainerDark = Color(0xFF3D4758)
val TotoroOnSecondaryContainerDark = Color(0xFFD8E3F8)
val TotoroTertiaryDark = Color(0xFFDABDE2)
val TotoroOnTertiaryDark = Color(0xFF3D2846)
val TotoroTertiaryContainerDark = Color(0xFF553F5D)
val TotoroOnTertiaryContainerDark = Color(0xFFF7D8FF)
val TotoroBackgroundDark = Color(0xFF1A1C1E)
val TotoroOnBackgroundDark = Color(0xFFE2E2E6)
val TotoroSurfaceDark = Color(0xFF1A1C1E)
val TotoroOnSurfaceDark = Color(0xFFE2E2E6)
val TotoroSurfaceVariantDark = Color(0xFF43474E)
val TotoroOnSurfaceVariantDark = Color(0xFFC3C6CF)
val TotoroOutlineDark = Color(0xFF8D9199)
val TotoroOutlineVariantDark = Color(0xFF43474E)

// Totoro Error
val TotoroError = Color(0xFFBA1A1A)
val TotoroOnError = Color(0xFFFFFFFF)
val TotoroErrorContainer = Color(0xFFFFDAD6)
val TotoroOnErrorContainer = Color(0xFF93000A)

// Asuka Theme Colors (Light)
val AsukaPrimary = Color(0xFF904A40)
val AsukaOnPrimary = Color(0xFFFFFFFF)
val AsukaPrimaryContainer = Color(0xFFFFDAD5)
val AsukaOnPrimaryContainer = Color(0xFF73342B)
val AsukaSecondary = Color(0xFF775651)
val AsukaOnSecondary = Color(0xFFFFFFFF)
val AsukaSecondaryContainer = Color(0xFFFFDAD5)
val AsukaOnSecondaryContainer = Color(0xFF5D3F3B)
val AsukaTertiary = Color(0xFF705C2E)
val AsukaOnTertiary = Color(0xFFFFFFFF)
val AsukaTertiaryContainer = Color(0xFFFCDFA6)
val AsukaOnTertiaryContainer = Color(0xFF564419)
val AsukaBackground = Color(0xFFFFF8F6)
val AsukaOnBackground = Color(0xFF231918)
val AsukaSurface = Color(0xFFFFF8F6)
val AsukaOnSurface = Color(0xFF231918)
val AsukaSurfaceVariant = Color(0xFFF5DDDA)
val AsukaOnSurfaceVariant = Color(0xFF534341)
val AsukaOutline = Color(0xFF857370)
val AsukaOutlineVariant = Color(0xFFD8C2BE)
val AsukaSurfaceContainer = Color(0xFFFCEAE7)

// Asuka Dark
val AsukaPrimaryDark = Color(0xFFFFB4A8)
val AsukaOnPrimaryDark = Color(0xFF561E16)
val AsukaPrimaryContainerDark = Color(0xFF73342B)
val AsukaOnPrimaryContainerDark = Color(0xFFFFDAD5)
val AsukaSecondaryDark = Color(0xFFE7BDB6)
val AsukaOnSecondaryDark = Color(0xFF442925)
val AsukaSecondaryContainerDark = Color(0xFF5D3F3B)
val AsukaOnSecondaryContainerDark = Color(0xFFFFDAD5)
val AsukaTertiaryDark = Color(0xFFDEC38C)
val AsukaOnTertiaryDark = Color(0xFF3E2E04)
val AsukaTertiaryContainerDark = Color(0xFF564419)
val AsukaOnTertiaryContainerDark = Color(0xFFFCDFA6)
val AsukaBackgroundDark = Color(0xFF1A1110)
val AsukaOnBackgroundDark = Color(0xFFF1DEDC)
val AsukaSurfaceDark = Color(0xFF1A1110)
val AsukaOnSurfaceDark = Color(0xFFF1DEDC)
val AsukaSurfaceVariantDark = Color(0xFF534341)
val AsukaOnSurfaceVariantDark = Color(0xFFD8C2BE)

// Itsuka Theme Colors (Light)
val ItsukaPrimary = Color(0xFF974800)
val ItsukaOnPrimary = Color(0xFFFFFFFF)
val ItsukaPrimaryContainer = Color(0xFFFE9245)
val ItsukaOnPrimaryContainer = Color(0xFF6B3100)
val ItsukaSecondary = Color(0xFF825334)
val ItsukaOnSecondary = Color(0xFFFFFFFF)
val ItsukaSecondaryContainer = Color(0xFFFDBF98)
val ItsukaOnSecondaryContainer = Color(0xFF794B2D)
val ItsukaTertiary = Color(0xFF755B0B) // Harmonized to Amber
val ItsukaOnTertiary = Color(0xFFFFFFFF)
val ItsukaTertiaryContainer = Color(0xFFFFDF95)
val ItsukaOnTertiaryContainer = Color(0xFF594400)
val ItsukaBackground = Color(0xFFFFF8F5)
val ItsukaOnBackground = Color(0xFF231A14)
val ItsukaSurface = Color(0xFFFFF8F5)
val ItsukaOnSurface = Color(0xFF231A14)
val ItsukaSurfaceVariant = Color(0xFFF9DDCE)
val ItsukaOnSurfaceVariant = Color(0xFF554338)
val ItsukaOutline = Color(0xFF897366)
val ItsukaOutlineVariant = Color(0xFFDCC1B3)
val ItsukaSurfaceContainer = Color(0xFFFDEAE1)

// Itsuka Dark
val ItsukaPrimaryDark = Color(0xFFFFB689)
val ItsukaOnPrimaryDark = Color(0xFF512400)
val ItsukaPrimaryContainerDark = Color(0xFF733500)
val ItsukaOnPrimaryContainerDark = Color(0xFFFE9245)
val ItsukaSecondaryDark = Color(0xFFF7B993)
val ItsukaOnSecondaryDark = Color(0xFF4C260B)
val ItsukaSecondaryContainerDark = Color(0xFF673C1F)
val ItsukaOnSecondaryContainerDark = Color(0xFFFDBF98)
val ItsukaTertiaryDark = Color(0xFFE6C36C) // Amber instead of Green
val ItsukaOnTertiaryDark = Color(0xFF3E2E00)
val ItsukaTertiaryContainerDark = Color(0xFF594400)
val ItsukaOnTertiaryContainerDark = Color(0xFFFFDF95)
val ItsukaBackgroundDark = Color(0xFF1A120E)
val ItsukaOnBackgroundDark = Color(0xFFF1DFD5)
val ItsukaSurfaceDark = Color(0xFF1A120E)
val ItsukaOnSurfaceDark = Color(0xFFF1DFD5)
val ItsukaSurfaceVariantDark = Color(0xFF554338)
val ItsukaOnSurfaceVariantDark = Color(0xFFDCC1B3)

// Kanade Theme Colors (Light)
val KanadePrimary = Color(0xFF353543)
val KanadeOnPrimary = Color(0xFFFFFFFF)
val KanadePrimaryContainer = Color(0xFF6C6C7B)
val KanadeOnPrimaryContainer = Color(0xFFFFFFFF)
val KanadeSecondary = Color(0xFF353636)
val KanadeOnSecondary = Color(0xFFFFFFFF)
val KanadeSecondaryContainer = Color(0xFF6D6D6D)
val KanadeOnSecondaryContainer = Color(0xFFFFFFFF)
val KanadeTertiary = Color(0xFF43474E)
val KanadeOnTertiary = Color(0xFFFFFFFF)
val KanadeTertiaryContainer = Color(0xFFDDE2F0)
val KanadeOnTertiaryContainer = Color(0xFF111C2B)
val KanadeBackground = Color(0xFFFCF8F9)
val KanadeOnBackground = Color(0xFF1C1B1C)
val KanadeSurface = Color(0xFFFCF8F9)
val KanadeOnSurface = Color(0xFF111112)
val KanadeSurfaceVariant = Color(0xFFE4E1E8)
val KanadeOnSurfaceVariant = Color(0xFF36363B)
val KanadeOutline = Color(0xFF535258)
val KanadeOutlineVariant = Color(0xFF6D6C72)
val KanadeSurfaceContainer = Color(0xFFEBE7E8)

// Kanade Dark
val KanadePrimaryDark = Color(0xFFC6C5D6)
val KanadeOnPrimaryDark = Color(0xFF2F2F3D)
val KanadePrimaryContainerDark = Color(0xFF474756)
val KanadeOnPrimaryContainerDark = Color(0xFFC6C5D6)
val KanadeSecondaryDark = Color(0xFFC7C7C7)
val KanadeOnSecondaryDark = Color(0xFF2F3030)
val KanadeSecondaryContainerDark = Color(0xFF474747)
val KanadeOnSecondaryContainerDark = Color(0xFFC7C7C7)
val KanadeTertiaryDark = Color(0xFFBCC7DB)
val KanadeOnTertiaryDark = Color(0xFF263141)
val KanadeTertiaryContainerDark = Color(0xFF3D4758)
val KanadeOnTertiaryContainerDark = Color(0xFFDDE2F0)
val KanadeBackgroundDark = Color(0xFF1C1B1C)
val KanadeOnBackgroundDark = Color(0xFFE6E1E2)
val KanadeSurfaceDark = Color(0xFF1C1B1C)
val KanadeOnSurfaceDark = Color(0xFFE6E1E2)
val KanadeSurfaceVariantDark = Color(0xFF48464C)
val KanadeOnSurfaceVariantDark = Color(0xFFC8C5CE)

// Mamimi Theme Colors (Light)
val MamimiPrimary = Color(0xFF465D91)
val MamimiOnPrimary = Color(0xFFFFFFFF)
val MamimiPrimaryContainer = Color(0xFFD9E2FF)
val MamimiOnPrimaryContainer = Color(0xFF2D4578)
val MamimiSecondary = Color(0xFF575E71)
val MamimiOnSecondary = Color(0xFFFFFFFF)
val MamimiSecondaryContainer = Color(0xFFDBE2F9)
val MamimiOnSecondaryContainer = Color(0xFF404659)
val MamimiTertiary = Color(0xFF725572)
val MamimiOnTertiary = Color(0xFFFFFFFF)
val MamimiTertiaryContainer = Color(0xFFFDD7FB)
val MamimiOnTertiaryContainer = Color(0xFF593E5A)
val MamimiBackground = Color(0xFFFAF8FF)
val MamimiOnBackground = Color(0xFF1A1B20)
val MamimiSurface = Color(0xFFFAF8FF)
val MamimiOnSurface = Color(0xFF1A1B20)
val MamimiSurfaceVariant = Color(0xFFE1E2EC)
val MamimiOnSurfaceVariant = Color(0xFF44464F)
val MamimiOutline = Color(0xFF757780)
val MamimiOutlineVariant = Color(0xFFC5C6D0)
val MamimiSurfaceContainer = Color(0xFFEEEDF4)

// Mamimi Dark
val MamimiPrimaryDark = Color(0xFFAFC6FF)
val MamimiOnPrimaryDark = Color(0xFF142E60)
val MamimiPrimaryContainerDark = Color(0xFF2D4578)
val MamimiOnPrimaryContainerDark = Color(0xFFD9E2FF)
val MamimiSecondaryDark = Color(0xFFBFC6DC)
val MamimiOnSecondaryDark = Color(0xFF293042)
val MamimiSecondaryContainerDark = Color(0xFF404659)
val MamimiOnSecondaryContainerDark = Color(0xFFDBE2F9)
val MamimiTertiaryDark = Color(0xFFDFBBDE)
val MamimiOnTertiaryDark = Color(0xFF412742)
val MamimiTertiaryContainerDark = Color(0xFF593E5A)
val MamimiOnTertiaryContainerDark = Color(0xFFFDD7FB)
val MamimiBackgroundDark = Color(0xFF1A1B20)
val MamimiOnBackgroundDark = Color(0xFFE2E2E9)
val MamimiSurfaceDark = Color(0xFF1A1B20)
val MamimiOnSurfaceDark = Color(0xFFE2E2E9)
val MamimiSurfaceVariantDark = Color(0xFF44464F)
val MamimiOnSurfaceVariantDark = Color(0xFFC5C6D0)

// Miku Theme Colors (Light)
val MikuPrimary = Color(0xFF00696D)
val MikuOnPrimary = Color(0xFFFFFFFF)
val MikuPrimaryContainer = Color(0xFF50C1C6)
val MikuOnPrimaryContainer = Color(0xFF004C4F)
val MikuSecondary = Color(0xFF3F6566)
val MikuOnSecondary = Color(0xFFFFFFFF)
val MikuSecondaryContainer = Color(0xFFBFE7E9)
val MikuOnSecondaryContainer = Color(0xFF43696B)
val MikuTertiary = Color(0xFF006874) // Harmonized Cyan-Blue
val MikuOnTertiary = Color(0xFFFFFFFF)
val MikuTertiaryContainer = Color(0xFFA1EFFF)
val MikuOnTertiaryContainer = Color(0xFF001F24)
val MikuBackground = Color(0xFFF5FAFA)
val MikuOnBackground = Color(0xFF171D1D)
val MikuSurface = Color(0xFFF5FAFA)
val MikuOnSurface = Color(0xFF171D1D)
val MikuSurfaceVariant = Color(0xFFD8E5E5)
val MikuOnSurfaceVariant = Color(0xFF3D4949)
val MikuOutline = Color(0xFF6D797A)
val MikuOutlineVariant = Color(0xFFBCC9C9)
val MikuSurfaceContainer = Color(0xFFEAEFEE)

// Miku Dark
val MikuPrimaryDark = Color(0xFF69D7DC)
val MikuOnPrimaryDark = Color(0xFF003739)
val MikuPrimaryContainerDark = Color(0xFF004F52)
val MikuOnPrimaryContainerDark = Color(0xFF87F3F8)
val MikuSecondaryDark = Color(0xFFA6CECF)
val MikuOnSecondaryDark = Color(0xFF0E3637)
val MikuSecondaryContainerDark = Color(0xFF274D4E)
val MikuOnSecondaryContainerDark = Color(0xFFC2EAEC)
val MikuTertiaryDark = Color(0xFF4ED8E8) // Lighter cyan-blue
val MikuOnTertiaryDark = Color(0xFF00363D)
val MikuTertiaryContainerDark = Color(0xFF004F58)
val MikuOnTertiaryContainerDark = Color(0xFFA1EFFF)
val MikuBackgroundDark = Color(0xFF171D1D)
val MikuOnBackgroundDark = Color(0xFFDEE3E3)
val MikuSurfaceDark = Color(0xFF171D1D)
val MikuOnSurfaceDark = Color(0xFFDEE3E3)
val MikuSurfaceVariantDark = Color(0xFF3D4949)
val MikuOnSurfaceVariantDark = Color(0xFFBCC9C9)

// Mion Theme Colors (Light)
val MionPrimary = Color(0xFF3B693A)
val MionOnPrimary = Color(0xFFFFFFFF)
val MionPrimaryContainer = Color(0xFFBCF0B4)
val MionOnPrimaryContainer = Color(0xFF235024)
val MionSecondary = Color(0xFF53634F) // Harmonized Gray-Green
val MionOnSecondary = Color(0xFFFFFFFF)
val MionSecondaryContainer = Color(0xFFD6E8CE)
val MionOnSecondaryContainer = Color(0xFF111F0F)
val MionTertiary = Color(0xFF386567) // Dark Teal-Green
val MionOnTertiary = Color(0xFFFFFFFF)
val MionTertiaryContainer = Color(0xFFBCEBEB)
val MionOnTertiaryContainer = Color(0xFF002021)
val MionBackground = Color(0xFFF7FBF1)
val MionOnBackground = Color(0xFF191D17)
val MionSurface = Color(0xFFF7FBF1)
val MionOnSurface = Color(0xFF191D17)
val MionSurfaceVariant = Color(0xFFDEE5D8)
val MionOnSurfaceVariant = Color(0xFF424940)
val MionOutline = Color(0xFF72796F)
val MionOutlineVariant = Color(0xFFC2C9BD)
val MionSurfaceContainer = Color(0xFFECEFE6)

// Mion Dark
val MionPrimaryDark = Color(0xFFA1D39A)
val MionOnPrimaryDark = Color(0xFF0B390F)
val MionPrimaryContainerDark = Color(0xFF235024)
val MionOnPrimaryContainerDark = Color(0xFFBCF0B4)
val MionSecondaryDark = Color(0xFFBACCC0)
val MionOnSecondaryDark = Color(0xFF253423)
val MionSecondaryContainerDark = Color(0xFF3B4B38)
val MionOnSecondaryContainerDark = Color(0xFFD6E8CE)
val MionTertiaryDark = Color(0xFFA0CFCF)
val MionOnTertiaryDark = Color(0xFF003738)
val MionTertiaryContainerDark = Color(0xFF1E4D4E)
val MionOnTertiaryContainerDark = Color(0xFFBCEBEB)
val MionBackgroundDark = Color(0xFF191D17)
val MionOnBackgroundDark = Color(0xFFE0E4DB)
val MionSurfaceDark = Color(0xFF191D17)
val MionOnSurfaceDark = Color(0xFFE0E4DB)
val MionSurfaceVariantDark = Color(0xFF424940)
val MionOnSurfaceVariantDark = Color(0xFFC2C9BD)

// Rikka Theme Colors (Light)
val RikkaPrimary = Color(0xFF68548D)
val RikkaOnPrimary = Color(0xFFFFFFFF)
val RikkaPrimaryContainer = Color(0xFFEBDCFF)
val RikkaOnPrimaryContainer = Color(0xFF503C74)
val RikkaSecondary = Color(0xFF635B70)
val RikkaOnSecondary = Color(0xFFFFFFFF)
val RikkaSecondaryContainer = Color(0xFFEADEF7)
val RikkaOnSecondaryContainer = Color(0xFF4B4358)
val RikkaTertiary = Color(0xFF385BA9) // Harmonized Royal Blue
val RikkaOnTertiary = Color(0xFFFFFFFF)
val RikkaTertiaryContainer = Color(0xFFDAE2FF)
val RikkaOnTertiaryContainer = Color(0xFF001946)
val RikkaBackground = Color(0xFFFEF7FF)
val RikkaOnBackground = Color(0xFF1D1B20)
val RikkaSurface = Color(0xFFFEF7FF)
val RikkaOnSurface = Color(0xFF1D1B20)
val RikkaSurfaceVariant = Color(0xFFE7E0EB)
val RikkaOnSurfaceVariant = Color(0xFF49454E)
val RikkaOutline = Color(0xFF7A757F)
val RikkaOutlineVariant = Color(0xFFCBC4CF)
val RikkaSurfaceContainer = Color(0xFFF2ECF4)

// Rikka Dark
val RikkaPrimaryDark = Color(0xFFD3BBFD)
val RikkaOnPrimaryDark = Color(0xFF39265B)
val RikkaPrimaryContainerDark = Color(0xFF503C74)
val RikkaOnPrimaryContainerDark = Color(0xFFEBDCFF)
val RikkaSecondaryDark = Color(0xFFCDC2DB)
val RikkaOnSecondaryDark = Color(0xFF342E41)
val RikkaSecondaryContainerDark = Color(0xFF4B4358)
val RikkaOnSecondaryContainerDark = Color(0xFFEADEF7)
val RikkaTertiaryDark = Color(0xFFB1C5FF) // Light Blue
val RikkaOnTertiaryDark = Color(0xFF002C71)
val RikkaTertiaryContainerDark = Color(0xFF1B428F)
val RikkaOnTertiaryContainerDark = Color(0xFFDAE2FF)
val RikkaBackgroundDark = Color(0xFF1D1B20)
val RikkaOnBackgroundDark = Color(0xFFE7E0E8)
val RikkaSurfaceDark = Color(0xFF1D1B20)
val RikkaOnSurfaceDark = Color(0xFFE7E0E8)
val RikkaSurfaceVariantDark = Color(0xFF49454E)
val RikkaOnSurfaceVariantDark = Color(0xFFCBC4CF)

// Sakura Theme Colors (Light)
val SakuraPrimary = Color(0xFF8C4A60)
val SakuraOnPrimary = Color(0xFFFFFFFF)
val SakuraPrimaryContainer = Color(0xFFFFD9E2)
val SakuraOnPrimaryContainer = Color(0xFF703348)
val SakuraSecondary = Color(0xFF74565F)
val SakuraOnSecondary = Color(0xFFFFFFFF)
val SakuraSecondaryContainer = Color(0xFFFFD9E2)
val SakuraOnSecondaryContainer = Color(0xFF5B3F47)
val SakuraTertiary = Color(0xFF81517E) // Harmonized Plum/Lavender
val SakuraOnTertiary = Color(0xFFFFFFFF)
val SakuraTertiaryContainer = Color(0xFFFFD7F9)
val SakuraOnTertiaryContainer = Color(0xFF340E37)
val SakuraBackground = Color(0xFFFFF8F8)
val SakuraOnBackground = Color(0xFF22191C)
val SakuraSurface = Color(0xFFFFF8F8)
val SakuraOnSurface = Color(0xFF22191C)
val SakuraSurfaceVariant = Color(0xFFF2DDE1)
val SakuraOnSurfaceVariant = Color(0xFF514347)
val SakuraOutline = Color(0xFF837377)
val SakuraOutlineVariant = Color(0xFFD5C2C6)
val SakuraSurfaceContainer = Color(0xFFFAEAED)

// Sakura Dark
val SakuraPrimaryDark = Color(0xFFFFB1C8)
val SakuraOnPrimaryDark = Color(0xFF541D32)
val SakuraPrimaryContainerDark = Color(0xFF703348)
val SakuraOnPrimaryContainerDark = Color(0xFFFFD9E2)
val SakuraSecondaryDark = Color(0xFFE3BDC6)
val SakuraOnSecondaryDark = Color(0xFF422931)
val SakuraSecondaryContainerDark = Color(0xFF5B3F47)
val SakuraOnSecondaryContainerDark = Color(0xFFFFD9E2)
val SakuraTertiaryDark = Color(0xFFF2B7ED) // Light Lavender
val SakuraOnTertiaryDark = Color(0xFF4D244D)
val SakuraTertiaryContainerDark = Color(0xFF663A65)
val SakuraOnTertiaryContainerDark = Color(0xFFFFD7F9)
val SakuraBackgroundDark = Color(0xFF22191C)
val SakuraOnBackgroundDark = Color(0xFFEFDFE1)
val SakuraSurfaceDark = Color(0xFF22191C)
val SakuraOnSurfaceDark = Color(0xFFEFDFE1)
val SakuraSurfaceVariantDark = Color(0xFF514347)
val SakuraOnSurfaceVariantDark = Color(0xFFD5C2C6)
