package com.shvarsman.coolinar.presentation.tour

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotPose

/**
 * Финальный диалог после прохождения тура — спрашивает, что делать с
 * демо-данными, которыми был заполнен тур. isProcessing блокирует повторные
 * нажатия, пока идёт удаление (может занять время на большом количестве
 * записей).
 */
@Composable
fun TourFinishDialog(
    isProcessing: Boolean,
    onDeleteAll: () -> Unit,
    onKeepRecipes: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotImage(
                    pose = MascotPose.EXCITED,
                    modifier = Modifier.height(120.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.tour_finish_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.tour_finish_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                if (isProcessing) {
                    CircularProgressIndicator()
                } else {
                    TextButton(onClick = onKeepRecipes, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.tour_finish_keep_recipes),
                            textAlign = TextAlign.Center,
                        )
                    }
                    TextButton(
                        onClick = onDeleteAll,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.tour_finish_delete_all),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}