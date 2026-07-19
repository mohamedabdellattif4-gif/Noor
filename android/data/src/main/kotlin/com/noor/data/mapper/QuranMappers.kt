package com.noor.data.mapper

import com.noor.core.common.text.ArabicNormalizer
import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.entity.SurahEntity
import com.noor.domain.model.Ayah
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah

internal fun SurahEntity.asDomain(): Surah = Surah(
    number = number,
    nameArabic = nameArabic,
    nameTransliterated = nameTransliterated,
    nameTranslated = nameTranslated,
    revelationType = RevelationType.valueOf(revelationType),
    ayahCount = ayahCount,
    startPage = startPage,
)

internal fun AyahEntity.asDomain(): Ayah = Ayah(
    id = id,
    surahNumber = surahNumber,
    numberInSurah = numberInSurah,
    textUthmani = textUthmani,
    juzNumber = juzNumber,
    hizbQuarter = hizbQuarter,
    pageNumber = pageNumber,
)

internal fun Surah.asEntity(): SurahEntity = SurahEntity(
    number = number,
    nameArabic = nameArabic,
    nameTransliterated = nameTransliterated,
    nameTranslated = nameTranslated,
    revelationType = revelationType.name,
    ayahCount = ayahCount,
    startPage = startPage,
)

internal fun Ayah.asEntity(): AyahEntity = AyahEntity(
    id = id,
    surahNumber = surahNumber,
    numberInSurah = numberInSurah,
    textUthmani = textUthmani,
    textSimple = ArabicNormalizer.normalize(textUthmani),
    juzNumber = juzNumber,
    hizbQuarter = hizbQuarter,
    pageNumber = pageNumber,
)
