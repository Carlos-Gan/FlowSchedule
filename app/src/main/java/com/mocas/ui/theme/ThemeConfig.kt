package com.mocas.ui.theme

import androidx.compose.ui.graphics.Color
import com.mocas.R

data class ThemeOption(
    val id: String,
    val nameRes: Int,
    val primary: Color,
    val primaryDark: Color,
    val background: Color,
    val backgroundDark: Color
)

object ThemeConfig {
    val themes = listOf(
        ThemeOption(
            id = "DEFAULT",
            nameRes = R.string.tema_estandar,
            primary = Primary,
            primaryDark = PrimaryDark,
            background = Background,
            backgroundDark = BackgroundDark
        ),
        ThemeOption(
            id = "TOTORO",
            nameRes = R.string.tema_totoro,
            primary = TotoroPrimary,
            primaryDark = TotoroPrimaryDark,
            background = TotoroBackground,
            backgroundDark = TotoroBackgroundDark
        ),
        ThemeOption(
            id = "ASUKA",
            nameRes = R.string.tema_asuka,
            primary = AsukaPrimary,
            primaryDark = AsukaPrimaryDark,
            background = AsukaBackground,
            backgroundDark = AsukaBackgroundDark
        ),
        ThemeOption(
            id = "ITSUKA",
            nameRes = R.string.tema_itsuka,
            primary = ItsukaPrimary,
            primaryDark = ItsukaPrimaryDark,
            background = ItsukaBackground,
            backgroundDark = ItsukaBackgroundDark
        ),
        ThemeOption(
            id = "KANADE",
            nameRes = R.string.tema_kanade,
            primary = KanadePrimary,
            primaryDark = KanadePrimaryDark,
            background = KanadeBackground,
            backgroundDark = KanadeBackgroundDark
        ),
        ThemeOption(
            id = "MAMIMI",
            nameRes = R.string.tema_mamimi,
            primary = MamimiPrimary,
            primaryDark = MamimiPrimaryDark,
            background = MamimiBackground,
            backgroundDark = MamimiBackgroundDark
        ),
        ThemeOption(
            id = "MIKU",
            nameRes = R.string.tema_miku,
            primary = MikuPrimary,
            primaryDark = MikuPrimaryDark,
            background = MikuBackground,
            backgroundDark = MikuBackgroundDark
        ),
        ThemeOption(
            id = "MION",
            nameRes = R.string.tema_mion,
            primary = MionPrimary,
            primaryDark = MionPrimaryDark,
            background = MionBackground,
            backgroundDark = MionBackgroundDark
        ),
        ThemeOption(
            id = "RIKKA",
            nameRes = R.string.tema_rikka,
            primary = RikkaPrimary,
            primaryDark = RikkaPrimaryDark,
            background = RikkaBackground,
            backgroundDark = RikkaBackgroundDark
        ),
        ThemeOption(
            id = "SAKURA",
            nameRes = R.string.tema_sakura,
            primary = SakuraPrimary,
            primaryDark = SakuraPrimaryDark,
            background = SakuraBackground,
            backgroundDark = SakuraBackgroundDark
        ),
        ThemeOption(
            id = "CRIMSON",
            nameRes = R.string.tema_crimson,
            primary = CrimsonPrimary,
            primaryDark = CrimsonPrimaryDark,
            background = CrimsonBackground,
            backgroundDark = CrimsonBackgroundDark
        ),
        ThemeOption(
            id = "COFFEE",
            nameRes = R.string.tema_coffee,
            primary = CoffeePrimary,
            primaryDark = CoffeePrimaryDark,
            background = CoffeeBackground,
            backgroundDark = CoffeeBackgroundDark
        ),
        ThemeOption(
            id = "MIDNIGHT",
            nameRes = R.string.tema_midnight,
            primary = MidnightPrimary,
            primaryDark = MidnightPrimaryDark,
            background = MidnightBackground,
            backgroundDark = MidnightBackgroundDark
        ),
        ThemeOption(
            id = "FOREST",
            nameRes = R.string.tema_forest,
            primary = ForestPrimary,
            primaryDark = ForestPrimaryDark,
            background = ForestBackground,
            backgroundDark = ForestBackgroundDark
        ),
        ThemeOption(
            id = "DYNAMIC",
            nameRes = R.string.tema_dinamico,
            primary = Color(0xFF4F6630), 
            primaryDark = Color(0xFFD3EABC),
            background = Color(0xFFFEFCF5),
            backgroundDark = Color(0xFF1B1C18)
        )
    )
}
