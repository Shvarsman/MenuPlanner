package com.shvarsman.coolinar.presentation.screens.cookselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shvarsman.coolinar.domain.model.MenuEntry
import com.shvarsman.coolinar.domain.usecase.menu.GetWeekMenuUseCase
import com.shvarsman.coolinar.presentation.screens.cooking.CookingDish
import com.shvarsman.coolinar.presentation.screens.cooking.CookingSessionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

/** Одно вхождение рецепта в меню с уже вычисленной реальной датой — единица списка выбора. */
data class CookableEntry(
    val entry: MenuEntry,
    val date: LocalDate
)

/** Группа записей одного дня для секции в списке. */
data class DayGroup(
    val date: LocalDate,
    val label: String,
    val entries: List<CookableEntry>
)

/** Итоговый выбор по одному рецепту: сколько и какие именно записи меню закрываем этой готовкой. */
data class SelectedItem(
    val recipeId: String,
    val menuEntryIds: List<String>
)

data class CookSelectionUiState(
    val includeNextWeek: Boolean = false,
    val dayGroups: List<DayGroup> = emptyList(),
    val selectedByRecipeId: Map<String, SelectedItem> = emptyMap(),
    val duplicateDialogEntries: List<CookableEntry>? = null,
    val navigateToCooking: Boolean = false
) {
    val selectedCount: Int get() = selectedByRecipeId.values.sumOf { it.menuEntryIds.size }
    val canStartCooking: Boolean get() = selectedByRecipeId.isNotEmpty()
}

@HiltViewModel
class CookSelectionViewModel @Inject constructor(
    getWeekMenu: GetWeekMenuUseCase,
    private val sessionHolder: CookingSessionHolder
) : ViewModel() {

    private val currentWeekStart =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val nextWeekStart = currentWeekStart.plusWeeks(1)

    private val currentWeekFlow = getWeekMenu(currentWeekStart)
    private val nextWeekFlow = getWeekMenu(nextWeekStart)

    private val _includeNextWeek = MutableStateFlow(false)
    private val _selectedByRecipeId = MutableStateFlow<Map<String, SelectedItem>>(emptyMap())
    private val _duplicateDialogEntries = MutableStateFlow<List<CookableEntry>?>(null)
    private val _navigateToCooking = MutableStateFlow(false)

    private val menuFlow = combine(
        currentWeekFlow, nextWeekFlow, _includeNextWeek
    ) { current, next, include -> Triple(current, next, include) }

    val uiState: StateFlow<CookSelectionUiState> = combine(
        menuFlow, _selectedByRecipeId, _duplicateDialogEntries, _navigateToCooking
    ) { (currentWeek, nextWeek, includeNext), selected, dialogEntries, navigate ->
        val today = LocalDate.now()
        val visible = buildList {
            addAll(currentWeek.map { CookableEntry(it, dateOf(it, currentWeekStart)) })
            if (includeNext) addAll(nextWeek.map { CookableEntry(it, dateOf(it, nextWeekStart)) })
        }

        val grouped = visible
            .groupBy { it.date }
            .toSortedMap(compareBy { sortKey(it, today) })
            .map { (date, entries) ->
                DayGroup(
                    date = date,
                    label = dayLabel(date, today),
                    entries = entries.sortedBy { it.entry.mealType.ordinal }
                )
            }

        CookSelectionUiState(
            includeNextWeek = includeNext,
            dayGroups = grouped,
            selectedByRecipeId = selected,
            duplicateDialogEntries = dialogEntries,
            navigateToCooking = navigate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CookSelectionUiState())

    fun toggleIncludeNextWeek(include: Boolean) {
        _includeNextWeek.value = include
    }

    /** Тап по карточке блюда: если рецепт встречается один раз — сразу переключаем выбор,
     * если несколько раз — открываем диалог выбора конкретных вхождений. */
    fun onEntryClick(clicked: CookableEntry) {
        val allVisible = uiState.value.dayGroups.flatMap { it.entries }
        val occurrences = allVisible.filter { it.entry.recipeId == clicked.entry.recipeId }

        if (occurrences.size <= 1) {
            toggleSingle(clicked)
        } else {
            _duplicateDialogEntries.value = occurrences
        }
    }

    private fun toggleSingle(entry: CookableEntry) {
        val recipeId = entry.entry.recipeId
        _selectedByRecipeId.value = _selectedByRecipeId.value.toMutableMap().apply {
            if (containsKey(recipeId)) remove(recipeId)
            else put(recipeId, SelectedItem(recipeId, listOf(entry.entry.id)))
        }
    }

    fun confirmDuplicateSelection(recipeId: String, chosenMenuEntryIds: List<String>) {
        _selectedByRecipeId.value = _selectedByRecipeId.value.toMutableMap().apply {
            if (chosenMenuEntryIds.isEmpty()) remove(recipeId)
            else put(recipeId, SelectedItem(recipeId, chosenMenuEntryIds))
        }
        _duplicateDialogEntries.value = null
    }

    fun dismissDuplicateDialog() {
        _duplicateDialogEntries.value = null
    }

    fun onStartCooking() {
        val dishes = uiState.value.selectedByRecipeId.values.map {
            CookingDish(recipeId = it.recipeId, menuEntryIds = it.menuEntryIds)
        }
        if (dishes.isEmpty()) return
        sessionHolder.set(dishes)
        _navigateToCooking.value = true
    }

    fun onNavigateToCookingConsumed() {
        _navigateToCooking.value = false
    }

    private fun dateOf(entry: MenuEntry, weekStart: LocalDate): LocalDate =
        weekStart.plusDays(entry.dayOfWeek.value - 1L)

    /** Сегодня и дни вперёд — по возрастанию; уже прошедшие дни текущей недели — в конец списка. */
    private fun sortKey(date: LocalDate, today: LocalDate): Long {
        val daysFromToday = ChronoUnit.DAYS.between(today, date)
        return if (daysFromToday < 0) daysFromToday + 10_000 else daysFromToday
    }

    private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Сегодня"
        today.plusDays(1) -> "Завтра"
        else -> {
            val dayName = date.dayOfWeek
                .getDisplayName(TextStyle.FULL, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
            "$dayName, ${date.dayOfMonth.toString().padStart(2, '0')}.${
                date.monthValue.toString().padStart(2, '0')
            }"
        }
    }
}