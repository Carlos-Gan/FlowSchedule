package com.mocas.ui.add.subject

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mocas.ui.components.OrganizationSelector
import com.mocas.ui.components.SubjectColorPicker

@Composable
fun PersonalizationCard(
    colorHex: String, onColorChange: (String) -> Unit,
    organizationTag: String, onTagChange: (String) -> Unit,
    isImportant: Boolean, onImportantChange: (Boolean) -> Unit
) {
    BaseCard("Personalización y Categorías") {
        Text("Color de la Materia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        SubjectColorPicker(selectedHex = colorHex, onColorSelected = onColorChange)

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        OrganizationSelector(
            selectedTag = organizationTag,
            isImportant = isImportant,
            onTagSelected = onTagChange,
            onImportantChanged = onImportantChange
        )
    }
}