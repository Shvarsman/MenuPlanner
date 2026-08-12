package com.shvarsman.coolinar.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.presentation.screens.common.GlassIconButton
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.PasswordField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val formMode by viewModel.formMode.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorRes by viewModel.errorRes.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(authState) {
        if (authState is com.shvarsman.coolinar.domain.model.AuthState.SignedIn) {
            onAuthSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
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
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.profile),
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
                onValueChange = { email = it; viewModel.clearError() }
            )
            Spacer(Modifier.height(12.dp))

            PasswordField(
                label = stringResource(R.string.profile_password),
                value = password,
                onValueChange = { password = it; viewModel.clearError() }
            )

            if (errorRes != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(errorRes!!),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.submit(email.trim(), password) },
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
                onClick = { viewModel.toggleFormMode() },
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
}