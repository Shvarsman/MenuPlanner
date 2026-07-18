package com.shvarsman.menuplanner.domain.model

sealed class StepContentItem {
    data class Text(val content: String) : StepContentItem()
    data class Image(val url: String) : StepContentItem()
    data class Timer(val minutes: Int) : StepContentItem()
}