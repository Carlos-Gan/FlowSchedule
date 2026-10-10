package com.mocas.ui.add.subject

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.ui.components.OrganizationSelector
import com.mocas.ui.components.SubjectColorPicker

@Composable
fun PersonalizationCard(
    colorHex: String, onColorChange: (String) -> Unit,
    organizationTag: String, onTagChange: (String) -> Unit,
    isImportant: Boolean, onImportantChange: (Boolean) -> Unit
) {
    BaseCard(
        title = stringResource(R.string.personalizacion_categorias_titulo),
        subtitle = stringResource(R.string.sub_personalizacion)
    ) {
        Text(
            text = stringResource(R.string.color_de_materia),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        SubjectColorPicker(selectedHex = colorHex, onColorSelected = onColorChange)

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        OrganizationSelector(
            selectedTag = organizationTag,
            isImportant = isImportant,
            onTagSelected = onTagChange,
            onImportantChanged = onImportantChange
        )
    }
}
