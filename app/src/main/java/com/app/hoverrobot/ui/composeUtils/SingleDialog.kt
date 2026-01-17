package com.app.hoverrobot.ui.composeUtils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.hoverrobot.R

@Composable
fun SingleDialog(
    @StringRes title: Int,
    @StringRes description: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.widthIn(max = 300.dp),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = CustomTextStyles.textStyle16Bold
            )
        },
        text = {
            Text(
                text = stringResource(description),
                textAlign = TextAlign.Start,
                style = CustomTextStyles.textStyle14Normal
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Ok",
                    color = MaterialTheme.colorScheme.primary,
                    style = CustomTextStyles.textStyle16Bold
                )
            }
        }
    )
}


@CustomPreview
@Composable
private fun SingleDialogPreview() {
    Column(Modifier.fillMaxSize()) {
        SingleDialog(
            title = R.string.firmware_dialog_title,
            description = R.string.firmware_dialog_description
        ) {}
    }
}