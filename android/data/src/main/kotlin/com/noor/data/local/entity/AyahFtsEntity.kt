package com.noor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4(contentEntity = AyahEntity::class)
@Entity(tableName = "ayahs_fts")
data class AyahFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Int,
    @ColumnInfo(name = "text_simple") val textSimple: String,
)
