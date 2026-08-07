package com.shvarsman.coolinar.presentation.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shvarsman.coolinar.R
import com.shvarsman.coolinar.domain.model.AuthState
import com.shvarsman.coolinar.presentation.screens.common.LabeledTextField
import com.shvarsman.coolinar.presentation.screens.common.MascotImage
import com.shvarsman.coolinar.presentation.screens.common.MascotSpeechBubble
import com.shvarsman.coolinar.presentation.screens.common.PasswordField
import com.shvarsman.coolinar.presentation.screens.profile.AuthFormMode
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int,
    @DrawableRes val mascotRes: Int
)

private val contentPages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_page1_title,
        descriptionRes = R.string.onboarding_page1_description,
        mascotRes = R.drawable.mascot_onboarding_fridge
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page2_title,
        descriptionRes = R.string.onboarding_page2_description,
        mascotRes = R.drawable.mascot_onboarding_cooking
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page3_title,
        descriptionRes = R.string.onboarding_page3_description,
        mascotRes = R.drawable.mascot_onboarding_planning
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val formMode by viewModel.formMode.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorRes by viewModel.errorRes.collectAsStateWithLifecycle()

    val pageCount = contentPages.size + 1
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            viewModel.finishOnboarding()
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                if (page < contentPages.size) {
                    ContentPage(contentPages[page])
                } else {
                    AuthStepPage(
                        formMode = formMode,
                        isSubmitting = isSubmitting,
                        errorRes = errorRes,
                        onSetFormMode = { viewModel.setFormMode(it) },
                        onSubmit = { email, password -> viewModel.submit(email, password) },
                        onClearError = { viewModel.clearError() },
                        onSkip = {
                            viewModel.finishOnboarding()
                            onFinished()
                        }
                    )
                }
            }

            if (pagerState.currentPage < contentPages.size) {
                TextButton(
                    onClick = { scope.launch { pagerState.scrollToPage(contentPages.size) } },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.onboarding_skip_intro))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
            }
        }

        if (pagerState.currentPage < contentPages.size) {
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

@Composable
private fun ContentPage(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MascotSpeechBubble(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                stringResource(page.titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MascotImage(
            drawableRes = page.mascotRes,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}
@Composable
private fun AuthStepPage(
    formMode: AuthFormMode,
    isSubmitting: Boolean,
    errorRes: Int?,
    onSetFormMode: (AuthFormMode) -> Unit,
    onSubmit: (email: String, password: String) -> Unit,
    onClearError: () -> Unit,
    onSkip: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = if (showForm) Arrangement.Center else Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showForm) {
            MascotSpeechBubble(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    stringResource(R.string.onboarding_final_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            MascotImage(
                drawableRes = R.drawable.mascot_onboarding_welcome,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Spacer(Modifier.height(24.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 32.dp)) {

            if (!showForm) {
                Button(
                    onClick = { onSetFormMode(AuthFormMode.SIGN_IN); showForm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_sign_in))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onSetFormMode(AuthFormMode.SIGN_UP); showForm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_sign_up))
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            } else {
                TextButton(
                    onClick = { showForm = false },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.back))
                }
                Spacer(Modifier.height(8.dp))

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
                        stringResource(errorRes),
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
                    onClick = {
                        onSetFormMode(if (formMode == AuthFormMode.SIGN_IN) AuthFormMode.SIGN_UP else AuthFormMode.SIGN_IN)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            if (formMode == AuthFormMode.SIGN_IN) R.string.profile_switch_to_sign_up
                            else R.string.profile_switch_to_sign_in
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }
    }
}