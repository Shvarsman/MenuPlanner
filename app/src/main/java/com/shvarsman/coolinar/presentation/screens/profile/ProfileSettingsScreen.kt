package com.shvarsman.coolinar.presentation.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.LineHeightStyle.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.FormCard
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.PasswordField

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

    val avatarUrl by viewModel.avatarUrl.collectAsStateWithLifecycle()
    val isUpdatingAvatar by viewModel.isUpdatingAvatar.collectAsStateWithLifecycle()

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.updateAvatar(it) } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings_title)) },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
            FormCard {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .align(CenterHorizontally)
                        .clip(CircleShape)
                        .clickable(enabled = !isUpdatingAvatar) {
                            avatarPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.profile_guest),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isUpdatingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                avatarPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) { Text(stringResource(R.string.profile_avatar_change)) }
                    }

                    if (avatarUrl != null && !isUpdatingAvatar) {
                        TextButton(
                            onClick = { viewModel.removeAvatar() }
                        ) {
                            Text(
                                stringResource(R.string.profile_avatar_remove),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            FormCard {
                LabeledTextField(
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

            FormCard {
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

                PasswordField(
                    label = stringResource(R.string.profile_current_password),
                    value = currentPassword,
                    onValueChange = { currentPassword = it; viewModel.clearPasswordError() }
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
                    label = stringResource(R.string.profile_new_password),
                    value = newPassword,
                    onValueChange = { newPassword = it; viewModel.clearPasswordError() }
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
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