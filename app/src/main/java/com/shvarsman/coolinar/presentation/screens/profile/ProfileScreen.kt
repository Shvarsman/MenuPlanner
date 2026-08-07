package com.shvarsman.coolinar.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.NavRow
import com.shvarsman.coolinar.presentation.screens.common.PasswordField
import com.shvarsman.coolinar.presentation.ui.theme.FloatingBottomBarClearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onOpenBackup: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val formMode by viewModel.formMode.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorRes by viewModel.errorRes.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + FloatingBottomBarClearance
                )
        ) {
            when (val state = authState) {
                is AuthState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is AuthState.SignedOut -> {
                    AuthForm(
                        formMode = formMode,
                        isSubmitting = isSubmitting,
                        errorRes = errorRes,
                        onSubmit = { email, password -> viewModel.submit(email, password) },
                        onToggleMode = { viewModel.toggleFormMode() },
                        onClearError = { viewModel.clearError() }
                    )
                }

                is AuthState.SignedIn -> {
                    SignedInContent(
                        displayName = displayName,
                        email = state.user.email,
                        onOpenProfileSettings = onOpenProfileSettings,
                        onOpenBackup = onOpenBackup,
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthForm(
    formMode: AuthFormMode,
    isSubmitting: Boolean,
    errorRes: Int?,
    onSubmit: (email: String, password: String) -> Unit,
    onToggleMode: () -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.AccountCircle,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(
                if (formMode == AuthFormMode.SIGN_IN) R.string.profile_sign_in else R.string.profile_sign_up
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(24.dp))

        LabeledTextField(
            label = stringResource(R.string.profile_email),
            value = email,
            onValueChange = { email = it; onClearError() }
        )
        Spacer(Modifier.height(12.dp))

        PasswordField(
            label = stringResource(R.string.profile_password),
            value = password,
            onValueChange = { password = it; onClearError() }
        )

        if (errorRes != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(errorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onSubmit(email.trim(), password) },
            enabled = !isSubmitting && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (formMode == AuthFormMode.SIGN_IN) R.string.profile_sign_in else R.string.profile_sign_up
                )
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (formMode == AuthFormMode.SIGN_IN) R.string.profile_switch_to_sign_up
                    else R.string.profile_switch_to_sign_in
                )
            )
        }
    }
}

@Composable
private fun SignedInContent(
    displayName: String?,
    email: String?,
    onOpenProfileSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    onSignOut: () -> Unit
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = displayName?.takeIf { it.isNotBlank() }
                        ?: email
                        ?: stringResource(R.string.profile_display_name_fallback),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (email != null) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        NavRow(
            icon = Icons.Filled.ManageAccounts,
            text = stringResource(R.string.profile_setup_button),
            onClick = onOpenProfileSettings
        )

        NavRow(
            icon = Icons.Filled.SettingsBackupRestore,
            text = stringResource(R.string.profile_backup_restore),
            onClick = onOpenBackup
        )

        NavRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            text = stringResource(R.string.profile_sign_out),
            tint = MaterialTheme.colorScheme.error,
            onClick = { showSignOutConfirm = true }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(stringResource(R.string.profile_sign_out_confirm_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showSignOutConfirm = false; onSignOut() }) {
                    Text(stringResource(R.string.profile_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}