package com.noor.data.adhkar

import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.Dhikr
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BundledAdhkarCatalog @Inject constructor() {
    fun load(): List<Dhikr> = ITEMS

    private companion object {
        val ITEMS: List<Dhikr> = listOf(
            Dhikr(
                id = "morning_ayat_al_kursi",
                category = AdhkarCategory.MORNING,
                title = "آية الكرسي",
                textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                reference = "سورة البقرة: 255",
                repeatCount = 1,
            ),
            Dhikr(
                id = "morning_ikhlas",
                category = AdhkarCategory.MORNING,
                title = "سورة الإخلاص",
                textArabic = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                reference = "سورة الإخلاص، وتقرأ مع الفلق والناس ثلاث مرات صباحًا",
                repeatCount = 3,
            ),
            Dhikr(
                id = "morning_falaq",
                category = AdhkarCategory.MORNING,
                title = "سورة الفلق",
                textArabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                reference = "سورة الفلق، سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "morning_nas",
                category = AdhkarCategory.MORNING,
                title = "سورة الناس",
                textArabic = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
                reference = "سورة الناس، سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "morning_asbahna",
                category = AdhkarCategory.MORNING,
                title = "أصبحنا وأصبح الملك لله",
                textArabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ. رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ. رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                reference = "صحيح مسلم",
                repeatCount = 1,
            ),
            Dhikr(
                id = "morning_bismillah",
                category = AdhkarCategory.MORNING,
                title = "بسم الله الذي لا يضر",
                textArabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ، وَهُوَ السَّمِيعُ الْعَلِيمُ",
                reference = "سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "evening_ayat_al_kursi",
                category = AdhkarCategory.EVENING,
                title = "آية الكرسي",
                textArabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                reference = "سورة البقرة: 255",
                repeatCount = 1,
            ),
            Dhikr(
                id = "evening_ikhlas",
                category = AdhkarCategory.EVENING,
                title = "سورة الإخلاص",
                textArabic = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                reference = "سورة الإخلاص، سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "evening_falaq",
                category = AdhkarCategory.EVENING,
                title = "سورة الفلق",
                textArabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                reference = "سورة الفلق، سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "evening_nas",
                category = AdhkarCategory.EVENING,
                title = "سورة الناس",
                textArabic = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
                reference = "سورة الناس، سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "evening_bismillah",
                category = AdhkarCategory.EVENING,
                title = "بسم الله الذي لا يضر",
                textArabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ، وَهُوَ السَّمِيعُ الْعَلِيمُ",
                reference = "سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "evening_amsayna",
                category = AdhkarCategory.EVENING,
                title = "أمسينا وأمسى الملك لله",
                textArabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ. رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا. رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                reference = "صحيح مسلم",
                repeatCount = 1,
            ),
            Dhikr(
                id = "evening_audhu",
                category = AdhkarCategory.EVENING,
                title = "أعوذ بكلمات الله التامات",
                textArabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                reference = "صحيح مسلم",
                repeatCount = 3,
            ),
            Dhikr(
                id = "after_prayer_tasbih",
                category = AdhkarCategory.AFTER_PRAYER,
                title = "تسبيح ما بعد الصلاة",
                textArabic = "سُبْحَانَ اللَّهِ ثلاثًا وثلاثين، وَالْحَمْدُ لِلَّهِ ثلاثًا وثلاثين، وَاللَّهُ أَكْبَرُ ثلاثًا وثلاثين، ثم: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                reference = "صحيح مسلم",
                repeatCount = 1,
            ),
            Dhikr(
                id = "after_prayer_astaghfirullah",
                category = AdhkarCategory.AFTER_PRAYER,
                title = "الاستغفار بعد الصلاة",
                textArabic = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ. اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
                reference = "صحيح مسلم",
                repeatCount = 1,
            ),
            Dhikr(
                id = "sleep_name",
                category = AdhkarCategory.SLEEP,
                title = "عند النوم",
                textArabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                reference = "صحيح البخاري",
                repeatCount = 1,
            ),
            Dhikr(
                id = "sleep_kafani",
                category = AdhkarCategory.SLEEP,
                title = "دعاء وضع الجنب",
                textArabic = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                reference = "سنن أبي داود والترمذي",
                repeatCount = 3,
            ),
            Dhikr(
                id = "waking_praise",
                category = AdhkarCategory.WAKING,
                title = "عند الاستيقاظ",
                textArabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                reference = "صحيح البخاري",
                repeatCount = 1,
            ),
            Dhikr(
                id = "general_sayyid_istighfar",
                category = AdhkarCategory.GENERAL,
                title = "سيد الاستغفار",
                textArabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                reference = "صحيح البخاري",
                repeatCount = 1,
            ),
            Dhikr(
                id = "general_hawqala",
                category = AdhkarCategory.GENERAL,
                title = "الحوقلة",
                textArabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                reference = "صحيح البخاري وصحيح مسلم",
                repeatCount = 1,
            ),
            Dhikr(
                id = "general_salawat",
                category = AdhkarCategory.GENERAL,
                title = "الصلاة على النبي ﷺ",
                textArabic = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                reference = "الأحزاب: 56، وصحيح مسلم في فضل الصلاة عليه",
                repeatCount = 1,
            ),
        )
    }
}
