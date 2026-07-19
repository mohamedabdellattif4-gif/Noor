package com.noor.domain.model

enum class AdhkarCategory(val key: String) {
    MORNING("morning"),
    EVENING("evening"),
    AFTER_PRAYER("after_prayer"),
    SLEEP("sleep"),
    WAKING("waking"),
    GENERAL("general");

    companion object {
        fun fromKey(value: String?): AdhkarCategory? = entries.firstOrNull { it.key == value }
    }
}
