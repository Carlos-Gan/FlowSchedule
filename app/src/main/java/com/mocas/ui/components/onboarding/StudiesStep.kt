package com.mocas.ui.components.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mocas.R
import com.mocas.ui.util.capitalizeFirstLetter

@Composable
fun StudiesStep(
    level: String, onLevelChange: (String) -> Unit,
    educationStage: String, onStageChange: (String) -> Unit,
    inst: String, onInstChange: (String) -> Unit,
    onDone: () -> Unit
) {
    StepLayout(
        title = stringResource(R.string.onboarding_paso_estudios_titulo),
        subtitle = stringResource(R.string.onboarding_paso_estudios_subtitulo),
        icon = Icons.Default.EditCalendar
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.onboarding_study_stage_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "HIGH_SCHOOL" to stringResource(R.string.onboarding_stage_high_school),
                        "UNIVERSITY" to stringResource(R.string.onboarding_stage_university),
                        "POSTGRAD" to stringResource(R.string.onboarding_stage_postgrad)
                    ).forEach { (value, label) ->
                        val selected = educationStage == value
                        Surface(
                            onClick = { onStageChange(value) }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            color = onboardingControlFill(selected),
                            border = onboardingControlBorder(selected),
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = level,
                onValueChange = { onLevelChange(capitalizeFirstLetter(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_carrera_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_carrera_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = inst,
                onValueChange = { onInstChange(capitalizeFirstLetter(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_institucion_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_institucion_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
            OnboardingInfoCallout(stringResource(R.string.onboarding_studies_hint))
        }
    }
}
