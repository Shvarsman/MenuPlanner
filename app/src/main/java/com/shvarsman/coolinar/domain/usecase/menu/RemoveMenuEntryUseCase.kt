package com.shvarsman.coolinar.domain.usecase.menu

import com.shvarsman.coolinar.domain.repository.MenuRepository
import javax.inject.Inject

class RemoveMenuEntryUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(entryId: Long) = repository.removeEntry(entryId)
}
