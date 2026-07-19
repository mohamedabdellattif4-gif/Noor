package com.noor.domain.model

data class Dhikr(
    val id: String,
    val category: AdhkarCategory,
    val title: String,
    val textArabic: String,
    val reference: String,
    val repeatCount: Int,
    val isFavorite: Boolean = false,
) {
    init {
        require(id.isNotBlank())
        require(title.isNotBlank())
        require(textArabic.isNotBlank())
        require(reference.isNotBlank())
        require(repeatCount > 0)
    }
}
