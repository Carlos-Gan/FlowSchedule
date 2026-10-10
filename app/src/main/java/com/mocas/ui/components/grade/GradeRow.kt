package com.mocas.ui.components.grade

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.data.local.GradeItemEntity

@Composable
fun GradeRow(item: GradeItemEntity, onDelete: (GradeItemEntity) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(format(item.score), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, stringResource(R.string.eliminar_calificacion_desc), modifier = Modifier.size(18.dp))
            }
        }
    }
}