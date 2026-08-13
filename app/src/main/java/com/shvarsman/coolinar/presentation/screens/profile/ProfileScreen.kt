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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.NavRow
import com.shvarsman.coolinar.presentation.screens.common.PasswordField
import com.shvarsman.coolinar.presentation.ui.theme.FloatingBottomBarClearance
import com.shvarsman.coolinar.presentation.ui.theme.molleFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onOpenBackup: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onOpenAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        fontFamily = molleFont
                    )
                },
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
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
                    SignedInContent(
                        displayName = stringResource(R.string.profile_display_name_guest),
                        email = null,
                        photoUrl = null,
                        onOpenProfileSettings = onOpenAuth,
                        onOpenBackup = onOpenBackup,
                        onSignOut = null
                    )
                }

                is AuthState.SignedIn -> {
                    SignedInContent(
                        displayName = state.user.displayName?.takeIf { it.isNotBlank() },
                        email = state.user.email,
                        photoUrl = state.user.photoUrl,
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
private fun SignedInContent(
    displayName: String?,
    email: String?,
    photoUrl: String?,
    onOpenProfileSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    onSignOut: (() -> Unit)?
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
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.profile_guest),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = displayName?.takeIf { it.isNotBlank() }
                        ?: email
                        ?: stringResource(R.string.profile_display_name_fallback),
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
            icon = ImageVector.vectorResource(R.drawable.profile_settings),
            text = stringResource(R.string.profile_setup_button),
            onClick = onOpenProfileSettings
        )

        NavRow(
            icon = ImageVector.vectorResource(R.drawable.backup_settings),
            text = stringResource(R.string.profile_backup_restore),
            onClick = onOpenBackup
        )

        if (onSignOut != null) {
            NavRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                text = stringResource(R.string.profile_sign_out),
                tint = MaterialTheme.colorScheme.error,
                onClick = { showSignOutConfirm = true }
            )
        }
    }

    if (showSignOutConfirm && onSignOut != null) {
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