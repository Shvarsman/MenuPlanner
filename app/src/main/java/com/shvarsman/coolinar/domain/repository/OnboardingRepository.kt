package com.shvarsman.coolinar.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val hasCompletedOnboarding: Flow<Boolean>

    /** Завершает онбординг. startTour = true — пользователь выбрал
     * "Продолжить без аккаунта", тур по приложению нужно запустить при
     * первом входе на HomeScreen. startTour = false — пользователь вошёл в
     * существующий аккаунт, у него уже есть данные, тур не нужен. */
    suspend fun setCompleted(startTour: Boolean)

    /** true, если тур запланирован, но ещё не был показан. */
    val isTourPending: Flow<Boolean>

    /** Помечает тур показанным — вызывается ровно один раз, когда тур
     * реально стартовал на HomeScreen, чтобы не запускать его повторно. */
    suspend fun consumeTourPending()
}