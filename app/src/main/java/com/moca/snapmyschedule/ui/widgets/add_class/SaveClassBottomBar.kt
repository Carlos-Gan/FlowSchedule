package com.moca.snapmyschedule.ui.widgets.add_class

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SaveClassBottomBar(
    buttonText: String,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
                .heightIn(min = 52.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector =
                    Icons.Default.Check,
                contentDescription = null
            )

            Text(
                text = buttonText,
                modifier =
                    Modifier.padding(start = 8.dp),
                style =
                    MaterialTheme.typography
                        .titleSmall
            )
        }
    }
}