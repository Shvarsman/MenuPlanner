package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.model.MenuEntry
import com.shvarsman.menuplanner.domain.repository.MenuRepository
import javax.inject.Inject

class AddMenuEntryUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(entry: MenuEntry): Long = repository.addEntry(entry)
}
