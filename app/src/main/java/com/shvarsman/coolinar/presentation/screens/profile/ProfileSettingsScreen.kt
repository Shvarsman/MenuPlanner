package com.shvarsman.coolinar.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.ui.theme.CornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val isSavingName by viewModel.isSavingName.collectAsStateWithLifecycle()
    val nameSaved by viewModel.nameSaved.collectAsStateWithLifecycle()

    val isChangingPassword by viewModel.isChangingPassword.collectAsStateWithLifecycle()
    val passwordErrorRes by viewModel.passwordErrorRes.collectAsStateWithLifecycle()
    val passwordChanged by viewModel.passwordChanged.collectAsStateWithLifecycle()

    var nameInput by remember(displayName) { mutableStateOf(displayName ?: "") }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = CornerShape) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsLabeledField(
                        label = stringResource(R.string.profile_display_name),
                        value = nameInput,
                        onValueChange = { nameInput = it }
                    )
                    if (nameSaved) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.profile_settings_name_saved),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveDisplayName(nameInput.trim()) },
                        enabled = !isSavingName && nameInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_settings_save))
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = CornerShape) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.profile_settings_password_section_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))

                    var currentPassword by remember { mutableStateOf("") }
                    var newPassword by remember { mutableStateOf("") }
                    var confirmPassword by remember { mutableStateOf("") }
                    var localMismatch by remember { mutableStateOf(false) }

                    SettingsPasswordField(
                        label = stringResource(R.string.profile_current_password),
                        value = currentPassword,
                        onValueChange = { currentPassword = it; viewModel.clearPasswordError() }
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsPasswordField(
                        label = stringResource(R.string.profile_new_password),
                        value = newPassword,
                        onValueChange = { newPassword = it; viewModel.clearPasswordError() }
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsPasswordField(
                        label = stringResource(R.string.profile_confirm_new_password),
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; viewModel.clearPasswordError() }
                    )

                    if (localMismatch) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.profile_error_passwords_dont_match),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (passwordErrorRes != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(passwordErrorRes!!),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (passwordChanged) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.profile_password_changed),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (newPassword != confirmPassword) {
                                localMismatch = true
                            } else {
                                localMismatch = false
                                viewModel.changePassword(currentPassword, newPassword)
                            }
                        },
                        enabled = !isChangingPassword && currentPassword.isNotBlank() &&
                                newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_settings_save))
                    }

                    LaunchedEffect(passwordChanged) {
                        if (passwordChanged) {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsLabeledField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SettingsPasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(R.string.profile_toggle_password_visibility)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
        }
    }
}