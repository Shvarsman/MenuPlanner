package com.shvarsman.menuplanner.domain.usecase.menu

import com.shvarsman.menuplanner.domain.repository.MenuRepository
import javax.inject.Inject

class RemoveMenuEntryUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(entryId: Long) = repository.removeEntry(entryId)
}
