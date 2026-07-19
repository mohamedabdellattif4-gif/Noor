package com.noor.data.adhkar

import com.noor.domain.model.AdhkarCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledAdhkarCatalogTest {
    private val items = BundledAdhkarCatalog().load()

    @Test
    fun bundledCatalogHasUniqueStableIdsAndValidContent() {
        assertEquals(items.size, items.map { it.id }.distinct().size)
        assertTrue(items.size >= 15)
        assertTrue(items.all { it.textArabic.isNotBlank() })
        assertTrue(items.all { it.reference.isNotBlank() })
        assertTrue(items.all { it.repeatCount > 0 })
    }

    @Test
    fun everyPublishedCategoryHasOfflineContent() {
        AdhkarCategory.entries.forEach { category ->
            assertTrue("No items for ${category.key}", items.any { it.category == category })
        }
    }
}
