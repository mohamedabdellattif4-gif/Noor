# مراجعة المرحلة الأولى — أساس المشروع والبناء

تاريخ المراجعة: 2026-07-17
النطاق: بنية Gradle، إصدارات أدوات البناء، Gradle Wrapper، المستودعات المركزية، إعدادات Android الأساسية، ومسار CI فقط.

لم تُغيَّر معمارية Noor المقفلة، ولم تُعدَّل وظائف التطبيق أو قاعدة البيانات أو مسارات التنقل أو عقود البيانات.

## 1. الملفات التي تم إنشاؤها

- `tools/verify_build_foundation.py`
  - بوابة تحقق مستقلة لإصدارات AGP وKotlin وKSP وAndroidX.
  - تتحقق من توافق Gradle Wrapper وإعدادات Android وJDK وSDK وCI.
  - تدعم وضع فحص المصدر داخل البيئة الحالية، ووضعًا صارمًا يشترط وجود Wrapper JAR في CI والإصدار.
- `docs/PHASE_1_FOUNDATION_REVIEW_2026-07-17.md`
  - تقرير هذه المرحلة ونتائجها ومخاطرها المتبقية.

## 2. الملفات التي تم تعديلها

- `.github/workflows/android-ci.yml`
- `README.md`
- `bootstrap-gradle-wrapper.ps1`
- `bootstrap-gradle-wrapper.sh`
- `gradle/libs.versions.toml`
- `gradle/wrapper/README.md`
- `gradlew`
- `gradlew.bat`
- `tools/verify_final.py`

## 3. الكود الإنتاجي الكامل

الكود الكامل موجود داخل أرشيف المرحلة، ولا توجد إضافات تجريبية أو `TODO` أو شيفرة وهمية.

أهم الإصلاحات المنفذة:

1. **حماية Gradle Wrapper**
   - فشل واضح عند غياب `gradle-wrapper.jar` بدل خطأ Java مبهم.
   - التحقق من SHA-256 الرسمي قبل تشغيل الـWrapper على Linux/macOS وWindows.
   - Bootstrap آمن عبر HTTPS مع مهلة زمنية، وإعادة محاولات محدودة، وملف مؤقت، وتنظيف مضمون، وتثبيت ذري بعد التحقق.
   - التحقق من SHA-256 لتوزيعة Gradle 9.4.1 داخل `gradle-wrapper.properties`.

2. **تحديثات AndroidX محدودة وآمنة**
   - `androidx.core:core-ktx`: من `1.18.0` إلى `1.19.0`.
   - Navigation Compose: من `2.9.7` إلى `2.9.8`.
   - إبقاء Compose BOM على `2026.06.01` لأنه كان بالفعل الإصدار المستقر الحالي في وقت المراجعة.

3. **بوابة بناء ثابتة**
   - تثبيت والتحقق من AGP `9.2.1` وGradle `9.4.1` وKotlin/KSP `2.3.10`.
   - التحقق من `compileSdk 37` و`targetSdk 36` وJava 17.
   - التحقق من المستودعات المركزية، Configuration Cache، Build Cache، Parallel Build، AndroidX، وR classes غير الانتقالية.
   - تشغيل بوابة الأساس قبل البناء الفعلي في GitHub Actions، ثم إعادة التحقق بعد تثبيت Wrapper JAR.

أوامر التحقق المعتمدة:

```bash
python3 tools/verify_build_foundation.py --allow-missing-wrapper-jar
python3 tools/verify_final.py
python3 tools/verify_hardening.py
```

وفي بيئة البناء الفعلية:

```bash
./bootstrap-gradle-wrapper.sh
python3 tools/verify_build_foundation.py --require-wrapper-jar
./gradlew clean :app:assembleDebug :app:lintDebug --warning-mode=all --stacktrace
```

## 4. القرارات الهندسية

### المشكلة الأولى: غياب Gradle Wrapper JAR

**السبب الجذري:** الأرشيف السابق يحتوي على launchers وخصائص Wrapper، لكنه لا يحتوي على الملف الثنائي `gradle-wrapper.jar`. لذلك لا يستطيع checkout نظيف بدء Gradle دون تنزيل شبكي.

**الإصلاح الأدنى الآمن:** لم يتم إدخال ملف غير موثوق أو إنشاء JAR بديل. تم تثبيت بصمة SHA-256 الرسمية في launchers وأدوات bootstrap وCI، مع اشتراط وجود الملف الصحيح في بوابة البناء الفعلية.

**التوافق:** لم يتغير إصدار Gradle أو شكل المشروع أو أوامر البناء المعتادة.

### المشكلة الثانية: تأخر إصداري Core KTX وNavigation Compose

**السبب الجذري:** بقيت النسخة على إصدارات مستقرة أقدم رغم توفر تحديثات patch/stable متوافقة، مما يزيد تراكم الصيانة ويفوّت إصلاحات المنصة والمكتبات.

**الإصلاح الأدنى الآمن:** تم تحديث المكتبتين فقط دون تغيير APIs أو architecture أو minSdk/targetSdk.

### عدم ترقية Kotlin أو AGP في هذه المرحلة

تم الإبقاء على Kotlin/KSP `2.3.10` مع AGP `9.2.1`. يستخدم AGP 9 تكامل Kotlin مدمجًا، كما أن AGP 9.2 موثق مع Kotlin `2.3.10`. الانتقال المنفرد إلى Kotlin 2.4.x سيصنع toolchain مختلطًا غير مختبر.

AGP `9.4.0` وKotlin `2.4.10` متاحان كإصدارات مستقرة أحدث، لكن ترقية AGP تتطلب Gradle `9.6.0` وتغيير Wrapper وبصماته، ثم تشغيل compile وKSP وHilt وRoom وLint والاختبارات كاملة. لذلك أُجِّلت هذه الهجرة المنسقة إلى مرحلة مستقلة بدل خلطها بإصلاح الأساس الأدنى.

### الحفاظ على التوافق الخلفي

- لم تتغير أسماء الحزم أو الوحدات.
- لم تتغير Room schema أو migrations.
- لم تتغير DataStore keys.
- لم تتغير Navigation routes.
- لم تتغير عقود Repository أو النماذج المتسلسلة.
- لم تتغير سياسات `minSdk` أو `targetSdk` أو `compileSdk` أو أرقام الإصدار.

## 5. المخاطر

1. **`gradle-wrapper.jar` ما زال غير موجود في الأرشيف.**
   - السبب: بيئة التنفيذ الحالية لا تستطيع الوصول إلى خادم Gradle، ولم يتم إدخال ملف ثنائي غير موثوق.
   - الإجراء الإلزامي: تنزيل الملف الرسمي عبر bootstrap ذي البصمة المثبتة، ثم إضافته إلى المستودع الحقيقي قبل اعتبار checkout ذاتي الاكتفاء.

2. **لم يُنفذ Android Gradle Build الحقيقي داخل هذه البيئة.**
   - لا يوجد Android SDK.
   - البيئة تستخدم JDK 21 بدل JDK 17 المطلوب للبناء المثبت.
   - تحليل DNS إلى `services.gradle.org` يفشل.

3. **مسار Windows لم يُشغّل Runtime.**
   - لا توجد PowerShell في البيئة الحالية.
   - تم فحص السكربت ساكنًا، بما في ذلك سلامة تمرير المسار، والتحقق من SHA-256، والتنظيف، والتثبيت الذري.

4. **ترقية AGP/Kotlin الأحدث مؤجلة عمدًا.**
   - ليست مشكلة تشغيلية في النسخة الحالية، لكنها دين تحديث مخطط يحتاج بوابة Android كاملة قبل الاعتماد.

## 6. مدى الجاهزية للإنتاج

| البوابة | النتيجة |
|---|---|
| مراجعة هيكل المشروع المقفل | ناجحة |
| فحص إعدادات Gradle/Android/CI | ناجح |
| فحص المصدر النهائي | ناجح |
| فحص Production Hardening | ناجح |
| اختبارات مسار Wrapper المفقود أو التالف | ناجحة |
| التوافق الخلفي للكود والبيانات | محفوظ |
| وجود Wrapper JAR داخل checkout | غير مكتمل |
| Android compile وKSP/Hilt/Room processors | غير منفذ في هذه البيئة |
| Android Lint الفعلي | غير منفذ في هذه البيئة |
| اختبارات الأجهزة/المحاكي | غير منفذة في هذه البيئة |

**حكم المرحلة:** أساس المصدر أصبح أكثر أمانًا وقابلية للتحقق مع الحفاظ الكامل على المعمارية والتوافق الخلفي. المرحلة الأولى ناجحة على مستوى المصدر، لكنها لا تمنح المشروع صفة Release Ready حتى يُضاف Wrapper JAR الرسمي وتنجح بوابة JDK 17 + Android SDK الفعلية في CI أو Android Studio.
