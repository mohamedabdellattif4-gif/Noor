from __future__ import annotations

import argparse
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "final-lesson3"))
sys.path.insert(0, str(Path(__file__).resolve().parent))

import build_final_lesson3 as base
import lesson4_data as data

for name in (
    "LESSON_TITLE", "AUTHOR", "GENERAL_IDEA", "ANALYSIS_NAMES", "TASTE_NAMES",
    "PARAGRAPHS", "MCQ", "COMPLETE_TEXT", "COMPLETE_MEANING", "GENERAL_QA",
    "ANALYSIS_Q", "TASTE_Q", "SCHOOLBOOK_END",
):
    setattr(base, name, getattr(data, name))


def add_intro(doc):
    base.add_title_band(doc, "مدخل إلى النص الشعري")
    base.add_box(doc, data.GENERAL_IDEA, fill=base.LIGHT_BLUE, border=base.BLUE, label="الفكرة العامة:")
    base.add_subtitle(doc, "النص كاملًا", color=base.ORANGE)
    base.add_box(doc, data.FULL_TEXT, fill="FFFBE6", border=base.GOLD, size=14)
    rows = [
        ["نوع النص", "نص شعري أدبي وطني إرشادي، يعتمد على العاطفة والصور والإيقاع والخطاب المباشر."],
        ["المجال", "الشباب والعمل والوطن وبناء الحضارة."],
        ["هدف الشاعر", "تقدير إنجاز الشباب وإخلاصهم، ثم تحميلهم مسؤولية المستقبل والحث على بناء حضارة عصرية قوية."],
        ["الجمهور المستهدف", "شباب الوطن خاصة، وكل من يشارك في إعدادهم وتمكينهم."],
        ["بنية النص", "ثلاثة مقاطع: قيمة الإنجاز، الإخلاص وإنكار الذات، ثم المسؤولية وبناء الحضارة."],
        ["المغزى", "مكانة الشباب تصنعها أعمالهم، والثقة بهم تكليف بالعلم والإخلاص وخدمة الوطن."],
    ]
    base.add_table(doc, ["العنصر", "البيان"], rows, header_fill=base.BLUE, alt_fill=base.LIGHT_BLUE, font_size=12.3)
    base.add_subtitle(doc, "الأهداف التعليمية", color=base.ORANGE)
    goals = [
        "قراءة الأبيات وفهم أفكارها وشرح كل بيت شرحًا واضحًا.",
        "تفسير المفردات وتحديد المعنى والمضاد والجمع والمفرد.",
        "تحليل كل مقطع وفق عناصر تحليل النص الثمانية.",
        "تذوق كل مقطع وفق عناصر التذوق الثلاثة عشر مع الشاهد والتفسير والأثر.",
        "تمييز الصور الحسية والأساليب والعاطفة والموسيقى والمعاني المباشرة والضمنية.",
        "الإجابة عن أسئلة كتاب المدرسة والتدريبات المتنوعة والشاملة.",
    ]
    for goal in goals:
        base.add_body_paragraph(doc, goal, bullet=True)


def paragraph_exercise_items(pdata, index):
    word0 = pdata["vocab"][0]
    word1 = pdata["vocab"][1]
    first_expr = pdata["expressions"][0]
    first_value = pdata["values"][0]
    analysis = pdata["analysis"]
    taste = pdata["taste"]
    book_q, book_a = pdata["book"][0]
    return [
        (f"هات معنى {word0[0]} ومضاد {word1[0]}.", f"معنى {word0[0]}: {word0[1]}. مضاد {word1[0]}: {word1[2]}."),
        ("صغ الفكرة الجزئية للمقطع بأسلوبك.", pdata["idea"]),
        ("اشرح أبيات المقطع شرحًا مركزًا يوضح رسالتها.", " ".join(pdata["explanation"][:3])),
        (book_q, book_a),
        ("حدد نوع النص في المقطع واذكر سمتين.", analysis[0][0]),
        ("حدد الفكرة الرئيسة ومعنى مباشرًا وآخر ضمنيًا.", analysis[1][0]),
        ("وضح بنية المقطع وعلاقة أبياته.", analysis[2][0]),
        (f"وضح دلالة التعبير: {first_expr[0]}.", first_expr[1]),
        ("استخرج علاقة بين الكلمات وأخرى بين الأبيات، ووضح أثرهما.", taste[2][0] + " " + taste[3][0]),
        ("حدد أسلوبًا واذكر نوعه وغرضه.", taste[4][0]),
        ("حدد صورة حسية ومصدرًا للموسيقى.", taste[8][0] + " " + taste[9][0]),
        ("احكم على المقطع من حيث وضوحه وتأثيره.", analysis[5][0]),
        (f"اقترح تطبيقًا حياتيًا لقيمة {first_value[0]}.", first_value[3]),
    ]


def add_poem_unit(doc, pdata, idx, answer_mode):
    base.add_title_band(doc, pdata["title"], fill=base.DARK_BLUE, page_break_before=True)
    base.add_box(doc, pdata["text"], fill="FFFBE6", border=base.GOLD, label="الأبيات:", size=14)
    base.add_box(doc, pdata["idea"], fill=base.LIGHT_BLUE, border=base.BLUE, label="الفكرة الجزئية:")

    base.add_subtitle(doc, "الشرح التفصيلي للأبيات", color=base.ORANGE)
    for point in pdata["explanation"]:
        base.add_body_paragraph(doc, point, bullet=True)

    base.add_subtitle(doc, "القاموس اللغوي الشامل", color=base.BLUE)
    base.add_table(doc, ["الكلمة أو التعبير", "المعنى", "المضاد", "الجمع", "المفرد"], pdata["vocab"], header_fill=base.BLUE, alt_fill=base.LIGHT_BLUE, font_size=11.2)

    base.add_subtitle(doc, "التعبيرات الجميلة ودلالاتها", color=base.RED)
    base.add_table(doc, ["التعبير", "الدلالة والتفسير والأثر"], pdata["expressions"], header_fill=base.RED, alt_fill=base.LIGHT_RED, font_size=11.7)

    base.add_subtitle(doc, "القيم والمبادئ والتطبيقات الحياتية", color=base.GREEN)
    base.add_table(doc, ["القيمة", "المقصود بها", "الدليل من النص", "التطبيق الحياتي"], pdata["values"], header_fill=base.GREEN, alt_fill=base.LIGHT_GREEN, font_size=11.2)

    base.add_qa_section(doc, "أسئلة الفهم والاستيعاب", pdata["qa"], answer_mode=answer_mode)
    base.add_qa_section(doc, "أسئلة كتاب المدرسة المدمجة", pdata["book"], answer_mode=answer_mode, source="كتاب المدرسة")

    base.add_subtitle(doc, "أولًا ـ عناصر تحليل النص مطبقة على المقطع الشعري", color=base.PURPLE, size=17)
    analysis_rows = [[name, content[0]] for name, content in zip(data.ANALYSIS_NAMES, pdata["analysis"])]
    base.add_table(doc, ["العنصر الصحيح", "التطبيق التفصيلي على المقطع"], analysis_rows, header_fill=base.PURPLE, alt_fill=base.LIGHT_PURPLE, font_size=11.3)

    base.add_subtitle(doc, "ثانيًا ـ عناصر تذوق النص مطبقة على المقطع الشعري", color=base.ORANGE, size=17)
    taste_rows = [[name, content[0]] for name, content in zip(data.TASTE_NAMES, pdata["taste"])]
    base.add_table(doc, ["العنصر الصحيح", "الشاهد والتفسير والأثر"], taste_rows, header_fill=base.ORANGE, alt_fill=base.LIGHT_ORANGE, font_size=11.1)

    base.add_subtitle(doc, f"تدريب شامل على المقطع الشعري {idx}", color=base.DARK_BLUE, size=17)
    for qnumb, (question, answer) in enumerate(paragraph_exercise_items(pdata, idx), 1):
        source = "كتاب المدرسة" if qnumb == 4 else None
        base.add_question(doc, str(qnumb), question, answer, source=source, answer_mode=answer_mode, lines=2)


def whole_lesson_exercise():
    return [
        ("اكتب الفكرة العامة للنص في جملة واحدة.", data.GENERAL_IDEA),
        ("قسم القصيدة إلى مقاطع، وضع عنوانًا لكل مقطع.", "إنجازات الشباب أبلغ من الثناء؛ إخلاص الشباب وإنكار الذات؛ الشباب صناع المستقبل والحضارة."),
        ("حدد أربع صفات للشباب مع دليل لكل صفة.", "أصحاب مآثر: أتم عقد مآثر. مخلصون: لم يمنوا. متواضعون: فما مدوا حناجرهم. قادة المستقبل: أنتم غدًا أهل الأمور."),
        ("وازن بين الثناء والعمل.", "الثناء كلام يقدر صاحبه، والعمل دليل حقيقي باق؛ لذلك قدم الشاعر الإنجاز على المدح."),
        ("وضح كيف كرم الوطن الشباب.", "قبل جهودهم وجعل التكريم تاجًا على هاماتهم."),
        ("دلل على إخلاص الشباب وإنكار الذات.", "فما مدوا حناجرهم ولا منوا على أوطانهم مجهودًا."),
        ("ما مسؤولية الشباب في المستقبل؟", "تولي القيادة وبناء حضارة عصرية قوية وخدمة الوطن."),
        ("حلل بنية القصيدة.", "حوار وثناء، ثم سبب ونتيجة في الإخلاص والتكريم، ثم خطاب وتكليف وتعليل في بناء المستقبل."),
        ("حدد الجمهور والغرض ووسيلتين للتأثير.", "الجمهور الشباب، والغرض الفخر والحث والإرشاد، والوسائل الخطاب المباشر والصور والأمر والتوكيد."),
        ("استخرج صورتين جميلتين واشرح أثرهما دون تصنيف بلاغي متقدم.", "جيد الزمان: يصور الزمن إنسانًا له عنق فيوحي بالخلود. ركن الحضارة: يصور الحضارة بناء قويًا فيوحي بالثبات."),
        ("استخرج صورة بصرية وأخرى سمعية.", "البصرية تاجًا على هاماتهم، والسمعية المنفية مدوا حناجرهم."),
        ("حدد العاطفة والجو العام.", "إعجاب وثقة واعتزاز، في جو متفائل حماسي وطني."),
        ("حدد ثلاثة أساليب وأغراضها.", "أتنظم استفهام للتمهيد، فابنوا أمر للحث، إن الذي قسم البلاد حباكم خبر مؤكد للتقرير."),
        ("وضح الموسيقى الخارجية والداخلية.", "الخارجية الوزن والقافية، والداخلية التكرار والتوازن والتجانس وتناسق الأصوات."),
        ("استخرج قيمة وطبقها في المدرسة.", "الإخلاص: أشارك في مشروع جماعي وأتم دوري دون من أو تفاخر."),
        ("وازن بين دور الكبار ودور الشباب.", "الكبار ينقلون الخبرة ويمهدون، والشباب يتسلمون المسؤولية ويطورون العمل بما يناسب العصر."),
        ("اكتب رسالة من خمسة أسطر إلى شباب الوطن.", "نموذج: اجعلوا العمل دليل محبتكم للوطن، وتعلموا علوم العصر، واحفظوا قيمكم، واعملوا في إخلاص وتواضع، وابنوا مستقبلًا قويًا يليق بوطنكم."),
        ("اقترح مشروعًا يطبق دعوة الشاعر.", "منصة طلابية رقمية لخدمة التعلم أو مشروع بيئي يستخدم التقنية والعمل التطوعي."),
        ("اكتب حكمًا نقديًا على النص.", "النص واضح مؤثر، جمع بين تقدير الشباب وتكليفهم، واعتمد لغة موحية وإيقاعًا قويًا، وأفكاره صالحة للتطبيق."),
        ("اكتب المغزى العام بأسلوبك.", "الشباب يبنون مكانتهم بالعمل المخلص، وعليهم أن يقودوا المستقبل بعلم ومسؤولية وحب للوطن."),
    ]


def add_end_exercises(doc, answer_mode):
    base.add_numbered_exercise(doc, "التدريبات الشاملة في نهاية الدرس ـ اختر الإجابة الصحيحة", data.MCQ, answer_mode, kind="mcq")
    base.add_numbered_exercise(doc, "أكمل من ألفاظ النص ـ عشرة أسئلة", data.COMPLETE_TEXT, answer_mode)
    base.add_numbered_exercise(doc, "أكمل بالمعنى المناسب ـ عشرة أسئلة", data.COMPLETE_MEANING, answer_mode)
    base.add_numbered_exercise(doc, "أجب عن الأسئلة الآتية ـ عشرة أسئلة", data.GENERAL_QA, answer_mode)
    base.add_numbered_exercise(doc, "تدريب شامل على عناصر تحليل النص", data.ANALYSIS_Q, answer_mode)
    base.add_numbered_exercise(doc, "تدريب شامل على عناصر تذوق النص", data.TASTE_Q, answer_mode)
    base.add_numbered_exercise(doc, "أسئلة كتاب المدرسة مجمعة ومدمجة", data.SCHOOLBOOK_END, answer_mode)

    base.add_title_band(doc, "تدريبات شاملة مستقلة على كل مقطع شعري", fill=base.PURPLE, page_break_before=True)
    for idx, pdata in enumerate(data.PARAGRAPHS, 1):
        base.add_subtitle(doc, f"التدريب الشامل على المقطع الشعري {idx}", color=base.PURPLE, size=16)
        for qnumb, (question, answer) in enumerate(paragraph_exercise_items(pdata, idx), 1):
            source = "كتاب المدرسة" if qnumb == 4 else None
            base.add_question(doc, f"{idx}.{qnumb}", question, answer, source=source, answer_mode=answer_mode, lines=2)

    base.add_numbered_exercise(doc, "التدريب النهائي الشامل على النص الشعري كله", whole_lesson_exercise(), answer_mode)


def validate_docx(path: Path, answer_mode: bool):
    assert path.exists() and path.stat().st_size > 50000, f"DOCX missing or too small: {path}"
    with zipfile.ZipFile(path) as zf:
        xml = zf.read("word/document.xml").decode("utf-8")
    required = [
        "عناصر تحليل النص مطبقة على المقطع الشعري",
        "عناصر تذوق النص مطبقة على المقطع الشعري",
        "أسئلة كتاب المدرسة المدمجة",
        "تدريبات شاملة مستقلة على كل مقطع شعري",
        "التدريب النهائي الشامل على النص الشعري كله",
        "١ ـ نوع النص", "١٣ ـ الموازنة الشعرية",
        "الكلمة أو التعبير", "المعنى", "المضاد", "الجمع", "المفرد",
    ]
    for phrase in required:
        assert phrase in xml, f"Missing required phrase: {phrase}"
    assert xml.count("w:bidiVisual") >= 45, "RTL table markers are insufficient"
    assert xml.count("أسئلة كتاب المدرسة المدمجة") == len(data.PARAGRAPHS), "Book questions not merged under every poem section"
    assert xml.count("عناصر تحليل النص مطبقة على المقطع الشعري") == len(data.PARAGRAPHS), "Analysis not placed after every poem section"
    assert xml.count("عناصر تذوق النص مطبقة على المقطع الشعري") == len(data.PARAGRAPHS), "Taste not placed after every poem section"
    for forbidden in ("استعارة مكنية", "استعارة تصريحية", "كناية عن", "تشبيه بليغ", "تشبيه تمثيلي"):
        assert forbidden not in xml, f"Advanced rhetorical label found: {forbidden}"
    if answer_mode:
        assert xml.count("الإجابة:") >= 110, "Answer key is incomplete"


def build(out_path: Path, answer_mode: bool):
    doc = base.Document()
    normal = doc.styles["Normal"]
    normal.font.name = base.FONT
    normal._element.rPr.rFonts.set(base.qn("w:ascii"), base.FONT)
    normal._element.rPr.rFonts.set(base.qn("w:hAnsi"), base.FONT)
    normal._element.rPr.rFonts.set(base.qn("w:cs"), base.FONT)
    normal.font.size = base.Pt(13)

    base.add_cover(doc, answer_mode)
    add_intro(doc)
    for idx, pdata in enumerate(data.PARAGRAPHS, 1):
        add_poem_unit(doc, pdata, idx, answer_mode)
    add_end_exercises(doc, answer_mode)
    base.add_header_footer(doc, answer_mode)

    core = doc.core_properties
    core.title = f"{data.LESSON_TITLE} ـ {'نموذج الإجابة' if answer_mode else 'نسخة الطالب'}"
    core.subject = "شرح وتحليل وتذوق وتدريبات النص الشعري للصف الثالث الإعدادي"
    core.author = "إعداد تعليمي"
    core.keywords = "تحية للشباب، أحمد شوقي، الصف الثالث الإعدادي، تحليل النص، تذوق النص، كتاب المدرسة"

    out_path.parent.mkdir(parents=True, exist_ok=True)
    doc.save(out_path)
    validate_docx(out_path, answer_mode)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--outdir", default="final-lesson4/output")
    args = parser.parse_args()
    outdir = Path(args.outdir)
    student = outdir / "تحية_للشباب_النظام_النهائي_المعتمد_نسخة_الطالب_Word2010.docx"
    answer = outdir / "نموذج_إجابة_تحية_للشباب_النظام_النهائي_المعتمد_Word2010.docx"
    build(student, False)
    build(answer, True)
    print(student)
    print(answer)


if __name__ == "__main__":
    main()
