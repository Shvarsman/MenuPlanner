package com.shvarsman.menuplanner.presentation.utils

sealed interface GroupedRow<out T, out C> {
    data class Header<C>(val category: C) : GroupedRow<Nothing, C>
    data class Item<T>(val value: T) : GroupedRow<T, Nothing>
}

fun <T, C> buildGroupedRows(
    items: List<T>,
    categoryOf: (T) -> C,
    categoryComparator: Comparator<C>
): List<GroupedRow<T, C>> {
    if (items.isEmpty()) return emptyList()
    return items
        .groupBy(categoryOf)
        .toSortedMap(categoryComparator)
        .flatMap { (category, group) ->
            buildList {
                add(GroupedRow.Header(category))
                group.forEach { add(GroupedRow.Item(it)) }
            }
        }
}

fun <T, C> buildGroupedRows(
    items: List<T>,
    categoryOf: (T) -> C,
    categoryOrder: (C) -> Int
): List<GroupedRow<T, C>> = buildGroupedRows(items, categoryOf, compareBy(categoryOrder))
