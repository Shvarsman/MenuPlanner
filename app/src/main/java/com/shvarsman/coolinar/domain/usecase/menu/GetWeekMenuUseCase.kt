package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.time.LocalDate

class GetWeekMenuUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(weekStart: LocalDate): Flow<List<MenuEntry>> = repository.observeWeekMenu(weekStart)
}