package com.moca.snapmyschedule.ui.widgets.import_schedule

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SelectedImagePreview(
    imageUri: Uri,
    onChangeImage: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType =
                                ImageView.ScaleType.FIT_CENTER

                            adjustViewBounds = true
                        }
                    },
                    update = { imageView ->
                        imageView.setImageURI(imageUri)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = "Imagen seleccionada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text =
                "Comprueba que los nombres, días y horas " +
                        "sean legibles antes de continuar.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRemoveImage,
                modifier = Modifier.weight(1f)
            ) {
                Text("Quitar")
            }

            Button(
                onClick = onChangeImage,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cambiar")
            }
        }
    }
}