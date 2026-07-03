package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeekMenuUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(): Flow<List<MenuEntry>> = repository.observeWeekMenu()
}
