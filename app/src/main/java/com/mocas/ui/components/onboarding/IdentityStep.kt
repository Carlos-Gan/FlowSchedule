package com.mocas.ui.components.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mocas.R
import com.mocas.ui.util.capitalizeFirstLetter

@Composable
fun IdentityStep(name: String, onNameChange: (String) -> Unit, onDone: () -> Unit) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_identidad_titulo),
        subtitle = stringResource(R.string.onboarding_paso_identidad_subtitulo),
        icon = Icons.Default.Person
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { onNameChange(capitalizeFirstLetter(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_field"),
            label = { Text(stringResource(R.string.onboarding_nombre_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_nombre_placeholder)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            supportingText = {
                if (name.isBlank()) Text(stringResource(R.string.onboarding_error_nombre))
            }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.onboarding_name_hint),
            modifier = Modifier.padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}