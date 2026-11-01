"""Writes the string resources (English and Russian) from one table, so the two never drift apart.

python tools/res.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"

S = [
    ("app_name", "Overwinter", "Зимовка"),
    ("tab_station", "Station", "Станция"),
    ("tab_log", "Barogram", "Барограмма"),
    ("tab_book", "Manual", "Наставление"),
    ("settings", "Settings", "Настройки"),
    ("next", "Next", "Дальше"),
    ("back", "Back", "Назад"),
    ("cancel", "Cancel", "Отмена"),
    ("done", "Done", "Готово"),
    ("date_pattern", "EEEE, d MMMM", "EEEE, d MMMM"),
    ("date_short", "d MMM", "d MMM"),
    ("callsign_default", "Body", "Тело"),
    ("station_x", "Station “%1$s”", "Станция «%1$s»"),
    ("shift_day", "Shift %1$d · day %2$d", "Смена %1$d · день %2$d"),
    ("hpa", "hPa", "гПа"),
    ("low_mark", "L", "Н"),
    ("high_mark", "H", "В"),
    # onboarding
    ("ob1", "You are wintering alone at a polar station. The station is your body. Three times a day comes a synoptic term: you read the instruments and send a short report to the mainland.",
     "Вы один зимуете на полярной станции. Станция — это ваше тело. Три раза в день наступает срок наблюдений: вы снимаете показания приборов и передаёте короткую сводку на Большую землю."),
    ("ob1b", "Bad days here are weather, not failures. Weather comes and goes; the observer stays at the post.",
     "Плохие дни здесь не провалы, а непогода. Она приходит и уходит, а наблюдатель остаётся на посту."),
    ("ob2_t", "The instruments", "Приборы"),
    ("ob2", "Everything is measured on the real meteorologist's scales, and each term takes ten seconds. Energy is the wind, from calm to hurricane. Mood is the cloud cover in eighths of the sky.",
     "Всё меряется настоящими шкалами метеоролога, и на срок уходит секунд десять. Силы — это ветер, от штиля до урагана. Настроение — облачность, в восьмых долях неба."),
    ("ob2_a", "A light breeze under a heavy sky: no strength, and gloomy inside.", "Слабый ветер, небо затянуто: сил нет, и на душе пасмурно."),
    ("ob2_b", "A fresh wind, some cloud: an ordinary working day.", "Свежий ветер, облачно с прояснениями: обычный рабочий день."),
    ("ob2_c", "A strong wind and a clear sky: I could move mountains.", "Крепкий ветер и ясное небо: горы сверну."),
    ("ob2_rest", "Pain is precipitation over a map of the body: drizzle, rain or a thunderstorm. In the morning you also log your sleep. Coffee, workouts and pills go into the weather box as instruments.",
     "Боль — это осадки на карте тела: морось, дождь или гроза. Утром ещё записывается сон. Кофе, тренировки и таблетки лежат в метеобудке как приборы."),
    ("ob2_rare", "The station grows with every term you keep, not with good days. An aurora comes after a cyclone has passed.",
     "Станция растёт от каждого срока, а не от хороших дней. Северное сияние приходит, когда циклон уже прошёл."),
    ("ob3_t", "Call sign", "Позывной"),
    ("ob3", "What should your station be called? The name goes at the top of every telegram.", "Как назовём станцию? Имя будет стоять в начале каждой телеграммы."),
    ("ob3_terms", "The terms are at 09:00, 15:00 and 21:00. You can move them in the settings, and a quiet notification will call you to each.",
     "Сроки в 09:00, 15:00 и 21:00. Их можно сдвинуть в настройках, а к каждому позовёт тихое уведомление."),
    ("ob3_private", "The journal lives only on this phone. The app has no internet access. A report for a doctor is made only when you ask for it.",
     "Журнал хранится только на этом телефоне, доступа в интернет у приложения нет. Сводку для врача вы делаете сами, когда нужно."),
    ("open_station", "Open the station", "Открыть станцию"),
    ("callsign", "Station call sign", "Позывной станции"),
    ("callsign_note", "Up to twelve letters. It's only for you and your telegrams.", "До двенадцати букв. Он нужен только вам и вашим телеграммам."),
    # station
    ("drift_t", "The station is snowed in", "Станцию замело"),
    ("drift_b", "No contact for %1$s. It happens on a wintering. One term, and we've dug ourselves out.",
     "Связи не было %1$s. На зимовке так бывает. Один срок, и станция откопана."),
    ("cyclone_now", "Cyclone %1$s over the station", "Над станцией циклон «%1$s»"),
    ("cyclone_now_b", "Bad weather for %1$s now. Cyclones pass; keep observing, and you'll see where this one ends.",
     "Непогода держится уже %1$s. Циклоны проходят. Продолжайте наблюдения, и будет видно, где он закончится."),
    ("term", "Synoptic term", "Срок наблюдений"),
    ("term_due", "%1$s, %2$s", "%1$s, %2$s"),
    ("term_due_b", "Time to read the instruments. Ten seconds.", "Пора снять показания. Это секунд десять."),
    ("term_start", "Start the term", "Начать срок"),
    ("term_next", "Next: %1$s at %2$s", "Следующий: %1$s в %2$s"),
    ("term_tomorrow", "Today's terms are done. The morning one is at %1$s.", "Сегодняшние сроки закрыты. Утренний в %1$s."),
    ("term_extra", "An extra observation", "Внеочередное наблюдение"),
    ("baro", "Barometer", "Барометр"),
    ("baro_3h", "%1$s in 3 h", "%1$s за 3 ч"),
    ("baro_24h", "%1$s hPa over the day", "%1$s гПа за сутки"),
    ("baro_warn", "The pressure is falling. Last time, days like this brought a headache: keep the pill close.",
     "Давление падает. В прошлый раз в такие дни болела голова: держите таблетку под рукой."),
    ("baro_yes", "The phone has a barometer. Once an hour the station writes down the pressure, to look for a link with how you feel.",
     "В телефоне есть барометр. Раз в час станция записывает давление, чтобы искать связь с самочувствием."),
    ("baro_no", "This phone has no barometer, so the station works without pressure.", "В этом телефоне нет барометра, поэтому станция работает без давления."),
    ("forecast", "Forecast", "Прогноз"),
    ("fc_ask", "What will tomorrow be like?", "Какой будет завтра?"),
    ("fc_later", "In the evening you can make a forecast for tomorrow, and the station will make its own.", "Вечером можно дать прогноз на завтра, и станция даст свой."),
    ("fc_today", "For today you forecast: %1$s. The day so far: %2$s.", "На сегодня вы обещали: %1$s. По факту пока: %2$s."),
    ("fc_hit", "The forecast came true.", "Прогноз оправдался."),
    ("fc_miss", "Not this time. Weather is hard to call.", "На этот раз нет. Погоду угадать трудно."),
    ("fc_tomorrow_mine", "Your forecast for tomorrow: %1$s.", "Ваш прогноз на завтра: %1$s."),
    ("fc_tomorrow_station", "The station's: %1$s.", "Прогноз станции: %1$s."),
    ("fc_score", "Your forecasts come true %1$d%% of the time (%2$d checked)", "Ваши прогнозы оправдываются на %1$d%% (проверено: %2$d)"),
    ("keeper", "Observer", "Наблюдатель"),
    ("next_building", "%1$s in %2$s", "%1$s — ещё %2$s"),
    ("all_built", "The station is complete. Every building stands.", "Станция построена целиком."),
    ("rare_open", "Rare phenomena: %1$d of %2$d", "Редкие явления: %1$d из %2$d"),
    ("rare_t", "Rare phenomena", "Редкие явления"),
    ("rare_b", "What has been seen from the station. Each one comes for a reason, and some only by luck.", "Что видели со станции. У каждого явления своя причина, а у некоторых только удача."),
    ("rare_unknown", "Not seen yet", "Пока не видели"),
    ("rare_seen", "Seen %1$s, last on %2$s", "Видели %1$s, последний раз — %2$s"),
    ("rare_new", "Seen from the station", "Видели со станции"),
    # observe
    ("obs_sub", "Read the instruments", "Снимаем показания"),
    ("wind_t", "Wind · energy", "Ветер · силы"),
    ("wind_lo", "calm: no strength", "штиль: сил нет"),
    ("wind_hi", "hurricane: unstoppable", "ураган: не остановить"),
    ("cloud_t", "Cloud · mood", "Облачность · настроение"),
    ("okta_x", "%1$d/8", "%1$d/8"),
    ("vis_t", "Visibility · how clear the head is", "Видимость · ясность в голове"),
    ("rain_t", "Precipitation · pain", "Осадки · боль"),
    ("rain_b", "Tap where it hurts: once for drizzle, twice for rain, three times for a storm, four to clear.",
     "Нажмите, где болит: один раз — морось, два — дождь, три — гроза, четыре — сухо."),
    ("rain_none", "Dry. Nothing hurts.", "Сухо. Ничего не болит."),
    ("rain_legend", "drizzle · rain · storm", "морось · дождь · гроза"),
    ("sleep_t", "Sleep last night", "Сон этой ночью"),
    ("hours_x", "%1$s h", "%1$s ч"),
    ("gear_t", "Weather box", "Метеобудка"),
    ("gear_b", "What was there today: tick the instruments.", "Что сегодня было: отметьте приборы."),
    ("gear_edit", "Set up", "Настроить"),
    ("gear_edit_long", "Choose the instruments", "Выбрать приборы"),
    ("gear_page_b", "Instruments are what you want to watch: coffee, pills, workouts. The station compares days with them and without them and looks for patterns.",
     "Приборы — это то, за чем хочется следить: кофе, таблетки, тренировки. Станция сравнивает дни с ними и без них и ищет закономерности."),
    ("gear_add", "A new instrument", "Новый прибор"),
    ("gear_add_hint", "For example: antihistamine", "Например: антигистаминное"),
    ("gear_add_btn", "Put it in the box", "Поставить в будку"),
    ("note_t", "Watch journal", "Вахтенный журнал"),
    ("note_hint", "A line, if you want one", "Строчка, если хочется"),
    ("send", "Send the report", "Передать сводку"),
    ("sent_t", "Radio room", "Радиорубка"),
    ("sent_head", "TELEGRAM · TO THE MAINLAND", "ТЕЛЕГРАММА · НА БОЛЬШУЮ ЗЕМЛЮ"),
    ("sent_legend", "How to read it: the station; the day, hour and term; 1 with wind, cloud and visibility; 2 with sleep; where it hurts; 4 with pressure; then the instruments.",
     "Как читать: станция; число, час и срок; группа 1 — ветер, облачность, видимость; 2 — сон; где болит; 4 — давление; в конце приборы."),
    ("sent_ok", "Report received", "Сводка принята"),
    ("sent_ok_b", "The mainland is in touch. Until the next term.", "Большая земля на связи. До следующего срока."),
    # log
    ("log_t", "Barogram", "Барограмма"),
    ("log_sub", "Thirty days in pencil", "Тридцать дней карандашом"),
    ("log_legend", "Pencil: how fair the day was. Dashes: the pressure. Below the dashed line it's a cyclone; marks at the bottom are pain.",
     "Карандаш — насколько ясным был день. Пунктир — давление. Ниже штриховой линии начинается циклон, внизу отмечена боль."),
    ("fronts_t", "Cyclones and highs", "Циклоны и антициклоны"),
    ("front_low", "Cyclone %1$s, %2$s", "Циклон «%1$s», %2$s"),
    ("front_low_now", "Cyclone %1$s, %2$s, still here", "Циклон «%1$s», %2$s, ещё над станцией"),
    ("front_high", "A high, %1$s", "Антициклон, %1$s"),
    ("log_empty", "No observations this month.", "В этом месяце наблюдений нет."),
    ("slept_x", "slept %1$s h", "сон %1$s ч"),
    ("obs_delete", "Delete this observation", "Удалить наблюдение"),
    ("obs_delete_yes", "Delete", "Удалить"),
    # book
    ("book_t", "Observer's manual", "Наставление наблюдателю"),
    ("book_sub", "What the journal says", "Что говорит журнал"),
    ("book_b", "The station compares days and writes down what repeats. Keep what's true, strike out what's chance. Kept rules help the station forecast.",
     "Станция сравнивает дни и записывает то, что повторяется. Верное оставьте, случайное вычеркните. Подтверждённые правила помогают станции с прогнозом."),
    ("book_empty", "Nothing has repeated yet. Patterns need about three weeks of observations, with days both with and without each instrument.",
     "Пока ничего не повторилось. Для закономерностей нужно недели три наблюдений, причём дни и с прибором, и без него."),
    ("book_days", "days in the journal: %1$d", "дней в журнале: %1$d"),
    ("book_struck", "Struck out: %1$d", "Вычеркнуто: %1$d"),
    ("book_restore", "Bring it back", "Вернуть"),
    ("rule_keep", "True", "Верно"),
    ("rule_strike", "Strike out", "Вычеркнуть"),
    ("rule_kept", "✓ Kept in the manual", "✓ Записано в наставление"),
    ("r_after_gear", "After “%1$s”,", "После отметки «%1$s»"),
    ("r_next_day", "the next day", "на следующий день"),
    ("r_after_short", "After a night shorter than six hours", "После ночи короче шести часов"),
    ("r_when_drop", "When the pressure falls by 4 hPa or more over a day,", "Когда давление за сутки падает на 4 гПа и больше,"),
    ("r_wind_more", "you have more energy: the wind averages %1$s against %2$s.", "сил больше: ветер в среднем %1$s против %2$s."),
    ("r_wind_less", "you have less energy: the wind averages %1$s against %2$s.", "сил меньше: ветер в среднем %1$s против %2$s."),
    ("r_cloud_less", "the mood is clearer: cloud %1$s/8 against %2$s/8.", "на душе яснее: облачность %1$s/8 против %2$s/8."),
    ("r_cloud_more", "the mood is cloudier: %1$s/8 against %2$s/8.", "на душе пасмурнее: облачность %1$s/8 против %2$s/8."),
    ("r_pain_more", "something hurts more often: %1$s%% of days against %2$s%%.", "чаще что-то болит: %1$s%% дней против %2$s%%."),
    ("r_pain_less", "something hurts less often: %1$s%% of days against %2$s%%.", "реже что-то болит: %1$s%% дней против %2$s%%."),
    ("r_sleep_more", "you sleep longer that night: %1$s h against %2$s.", "ночью спится дольше: %1$s ч против %2$s."),
    ("r_sleep_less", "you sleep less that night: %1$s h against %2$s.", "ночью спится меньше: %1$s ч против %2$s."),
    ("r_evidence", "%1$d days with it, %2$d without", "дней с этим: %1$d, без: %2$d"),
    ("score_t", "How forecasts come true", "Оправдываемость прогнозов"),
    ("score_none", "No forecasts checked yet. Make one in the evening on the Station tab.", "Проверенных прогнозов пока нет. Дайте прогноз вечером на вкладке «Станция»."),
    ("score_mine", "yours", "ваши"),
    ("score_station", "the station's", "станции"),
    ("shifts_t", "Shifts", "Смены"),
    ("shift_now", "Shift %1$d: day %2$d of %3$d", "Смена %1$d: день %2$d из %3$d"),
    ("shift_old", "Shift %1$d, %2$s – %3$s", "Смена %1$d, %2$s – %3$s"),
    ("shift_sum", "%1$d terms, %2$d cyclones, %3$d%% fair days", "сроков: %1$d, циклонов: %2$d, ясных дней: %3$d%%"),
    ("report_t", "For a doctor", "Для врача"),
    ("report_b", "A plain PDF without the polar words: energy, mood, pain by body part, sleep, what you took, and the days in a table.",
     "Обычный PDF без полярных слов: силы, настроение, боль по частям тела, сон, что принимали, и дни таблицей."),
    ("report_make", "Make a PDF and share it", "Сделать PDF и отправить"),
    ("report_empty", "No observations in this period.", "За этот период наблюдений нет."),
    # pdf
    ("pdf_title", "Wellbeing diary", "Дневник самочувствия"),
    ("pdf_period", "%1$s – %2$s · %3$s · made with Overwinter", "%1$s – %2$s · %3$s · сделано в «Зимовке»"),
    ("pdf_scales", "Scales: energy 0–12 (0 none, 12 a lot), mood heaviness 0–8 (0 light, 8 very low), clarity of mind 0–3 (0 fog, 3 sharp), pain 1 mild, 2 moderate, 3 severe.",
     "Шкалы: силы 0–12 (0 — нет совсем, 12 — очень много), тяжесть настроения 0–8 (0 — легко, 8 — очень тяжело), ясность головы 0–3 (0 — туман, 3 — ясно), боль 1 — слабая, 2 — умеренная, 3 — сильная."),
    ("pdf_summary", "Summary", "Итоги"),
    ("pdf_days", "Days with records: %1$d of %2$d; records: %3$d.", "Дней с записями: %1$d из %2$d, записей: %3$d."),
    ("pdf_energy", "Average energy: %1$s of 12.", "Средние силы: %1$s из 12."),
    ("pdf_mood", "Average mood heaviness: %1$s of 8.", "Средняя тяжесть настроения: %1$s из 8."),
    ("pdf_clarity", "Average clarity of mind: %1$s of 3.", "Средняя ясность головы: %1$s из 3."),
    ("pdf_sleep", "Average sleep: %1$s h; nights shorter than 6 h: %2$d.", "Средний сон: %1$s ч, ночей короче 6 ч: %2$d."),
    ("pdf_pain", "Pain", "Боль"),
    ("pdf_pain_zone", "%1$s: %2$d days (mild %3$d, moderate %4$d, severe %5$d)", "%1$s: дней — %2$d (слабая %3$d, умеренная %4$d, сильная %5$d)"),
    ("pdf_pain_none", "No pain recorded.", "Боль не отмечалась."),
    ("pdf_chart", "Energy and mood by day", "Силы и настроение по дням"),
    ("pdf_chart_legend", "Red: energy (higher is more). Blue: mood (higher is lighter). Dots below: moderate or severe pain.",
     "Красная линия — силы (выше — больше). Синяя — настроение (выше — легче). Точки внизу — умеренная или сильная боль."),
    ("pdf_gear", "Taken or done (days)", "Что принимали и делали (дней)"),
    ("pdf_rules", "Patterns noticed in the diary", "Закономерности из дневника"),
    ("pdf_table", "Day by day", "По дням"),
    ("pdf_footer", "Page %1$d", "Страница %1$d"),
    ("pdf_file", "Wellbeing %1$s", "Самочувствие %1$s"),
    # notifications
    ("n_channel", "Synoptic terms", "Сроки наблюдений"),
    ("n_morning", "Morning term", "Утренний срок"),
    ("n_day", "Afternoon term", "Дневной срок"),
    ("n_evening", "Evening term", "Вечерний срок"),
    ("n_body", "Time to read the instruments and send the report.", "Пора снять показания и передать сводку."),
    # settings
    ("terms_t", "Terms", "Сроки"),
    ("remind", "Call me to the terms", "Звать на сроки"),
    ("remind_b", "A quiet notification, only if the term isn't done yet", "Тихое уведомление, только если срок ещё не закрыт"),
    ("sound", "The telegraph key", "Телеграфный ключ"),
    ("sound_b", "A little Morse when the report goes out", "Немного морзянки, когда уходит сводка"),
    ("theme", "Theme", "Тема"),
    ("theme_system", "As the system", "Как в системе"),
    ("theme_light", "Polar day", "Полярный день"),
    ("theme_dark", "Polar night", "Полярная ночь"),
    ("language", "Language", "Язык"),
    ("lang_system", "System", "Системный"),
    ("lang_ru", "Русский", "Русский"),
    ("lang_en", "English", "English"),
    ("about_t", "Where it all lives", "Где всё хранится"),
    ("about_b", "Only on this phone. The app has no internet access and no accounts. The barometer is read on the phone itself. A report leaves the phone only when you share it.",
     "Только на этом телефоне. Доступа в интернет и аккаунтов у приложения нет. Барометр читается самим телефоном. Сводка для врача уходит с телефона, только когда вы её отправляете."),
    ("version", "Version %1$s", "Версия %1$s"),
    ("community", "Community", "Сообщество"),
    ("community_text", "Overwinter is made by one person. Bugs, ideas and news go through the StillCraft bot in Telegram.",
     "«Зимовку» делает один человек. Ошибки, идеи и новости — через бота StillCraft в Telegram."),
    ("community_bug", "Report a bug", "Сообщить об ошибке"),
    ("community_bug_sub", "Something broke or looks wrong", "Что-то сломалось или выглядит не так"),
    ("community_idea", "Suggest an idea", "Предложить идею"),
    ("community_idea_sub", "What would make it better", "Что сделало бы приложение лучше"),
    ("community_follow", "Follow updates", "Следить за обновлениями"),
    ("community_follow_sub", "A message when a new version is out", "Сообщение, когда выйдет новая версия"),
    ("community_chat", "StillCraft chat", "Чат StillCraft"),
    ("community_chat_sub", "Talk to other people who use the apps", "Поговорить с теми, кто пользуется приложениями"),
]

P = [
    ("terms", ("%1$d term observed", "%1$d terms observed"), ("%1$d срок наблюдений", "%1$d срока наблюдений", "%1$d сроков наблюдений")),
    ("forecasts", ("%1$d forecast checked", "%1$d forecasts checked"), ("проверен %1$d прогноз", "проверено %1$d прогноза", "проверено %1$d прогнозов")),
    ("days", ("%1$d day", "%1$d days"), ("%1$d день", "%1$d дня", "%1$d дней")),
    ("times", ("once", "%1$d times"), ("%1$d раз", "%1$d раза", "%1$d раз")),
]

A = [
    ("slots", ["Morning", "Afternoon", "Evening"], ["Утро", "День", "Вечер"]),
    ("slot_terms", ["Morning term", "Afternoon term", "Evening term"], ["Утренний срок", "Дневной срок", "Вечерний срок"]),
    ("beaufort", ["calm", "light air", "light breeze", "gentle breeze", "moderate breeze", "fresh breeze", "strong breeze",
                  "near gale", "gale", "strong gale", "storm", "violent storm", "hurricane"],
                 ["штиль", "тихий", "лёгкий", "слабый", "умеренный", "свежий", "сильный",
                  "крепкий", "очень крепкий", "шторм", "сильный шторм", "жестокий шторм", "ураган"]),
    ("beaufort_body", [
        "Flat out, no strength at all.", "Barely moving.", "A little strength, enough for the basics.", "Slow, but going.",
        "An ordinary pace.", "Things are getting done.", "Plenty of strength.", "Full sails.", "Hard to sit still.",
        "Taking on everything at once.", "Unstoppable.", "Energy to spare, almost too much.", "I could move mountains.",
    ], [
        "Лежу пластом, сил нет совсем.", "Еле шевелюсь.", "Сил немного, хватает на самое нужное.", "Медленно, но иду.",
        "Обычный рабочий ход.", "Дело спорится.", "Сил много.", "Паруса полные.", "Трудно усидеть на месте.",
        "Берусь за всё сразу.", "Не остановить.", "Энергии через край, даже слишком.", "Горы сверну.",
    ]),
    ("okta", ["clear", "almost clear", "few clouds", "scattered", "half the sky", "broken", "mostly cloudy", "nearly overcast", "overcast"],
             ["ясно", "почти ясно", "малооблачно", "небольшая облачность", "полнеба", "облачно с прояснениями", "облачно", "почти пасмурно", "пасмурно"]),
    ("okta_mood", [
        "Light and easy.", "Almost light.", "A small cloud, nothing much.", "Mostly fine.", "Half and half.",
        "More grey than blue.", "Heavy.", "Very heavy, a gap here and there.", "All grey, no gaps.",
    ], [
        "Легко на душе.", "Почти легко.", "Маленькое облачко, ничего страшного.", "В целом хорошо.", "Пополам.",
        "Серого больше, чем синего.", "Тяжело.", "Очень тяжело, просветы редкие.", "Всё серое, просветов нет.",
    ]),
    ("vis", ["fog", "haze", "good", "excellent"], ["туман", "дымка", "хорошая", "отличная"]),
    ("zones", ["Head", "Throat", "Chest", "Stomach", "Back", "Arms", "Legs", "Whole body"],
              ["Голова", "Горло", "Грудь", "Живот", "Спина", "Руки", "Ноги", "Всё тело"]),
    ("zones_plain", ["Head", "Throat, neck", "Chest", "Abdomen", "Back", "Arms", "Legs", "Whole body, aches"],
                    ["Голова", "Горло, шея", "Грудь", "Живот", "Спина", "Руки", "Ноги", "Всё тело, ломота"]),
    ("zone_codes", ["HED", "THR", "CHS", "BEL", "BAK", "ARM", "LEG", "ALL"], ["ГОЛ", "ГРЛ", "ГРД", "ЖИВ", "СПН", "РУК", "НОГ", "ВСЕ"]),
    ("rain_levels", ["", "drizzle", "rain", "storm"], ["", "морось", "дождь", "гроза"]),
    ("sky", ["clear", "changeable", "bad weather"], ["ясно", "переменно", "ненастье"]),
    ("gear_builtin", ["Coffee", "Alcohol", "Workout", "Walk", "Pill", "Stress", "Screen late", "Sweets", "Period"],
                     ["Кофе", "Алкоголь", "Тренировка", "Прогулка", "Таблетка", "Стресс", "Экран допоздна", "Сладкое", "Месячные"]),
    ("buildings", ["Living hut", "Weather screen", "Anemometer", "Flag mast", "Radio mast", "Wind turbine", "Dog Purga",
                   "Banya", "Weather balloon", "Greenhouse", "Beacon", "Observatory dome", "Helipad", "Icebreaker at the pier"],
                  ["Жилой балок", "Метеобудка", "Флюгер", "Мачта с флагом", "Радиомачта", "Ветряк", "Собака Пурга",
                   "Баня", "Аэрологический шар", "Теплица", "Маяк", "Купол обсерватории", "Вертолётная площадка", "Ледокол у причала"]),
    ("ranks", ["Observer", "Senior observer", "Weather technician", "Forecaster", "Head of station"],
              ["Наблюдатель", "Старший наблюдатель", "Техник-метеоролог", "Синоптик", "Начальник станции"]),
    ("rare_names", ["Aurora", "Halo", "Sun dog", "Arctic fox", "Polar bear", "Snowy owl", "Reindeer tracks", "Icebreaker with the mail", "Green flash", "Radiogram"],
                   ["Северное сияние", "Гало", "Ложное солнце", "Песец у окна", "Белый медведь", "Полярная сова", "Следы оленей", "Ледокол с почтой", "Зелёный луч", "Радиограмма"]),
    ("rare_texts", [
        "The cyclone has passed, and the sky lit up over the station.",
        "A ring round the morning sun: a clear head and a good wind.",
        "Two suns at once: a clear sky, a strong wind and nothing hurting.",
        "A fox came up to the lit window in the evening.",
        "A bear walked along the horizon and didn't come close. Luck.",
        "An owl sat on the mast while you were awake at night.",
        "You came back after a long break and found tracks round the station. Welcome back.",
        "Another thirty days on the station. The icebreaker brought the mail.",
        "Five fair days in a row, and a green flash at sunset.",
        "The mainland confirmed your first rule of the manual.",
    ], [
        "Циклон прошёл, и над станцией загорелось небо.",
        "Кольцо вокруг утреннего солнца: голова ясная, ветер хороший.",
        "Два солнца сразу: небо чистое, ветер крепкий, и ничего не болит.",
        "Вечером песец подошёл к светлому окну.",
        "Медведь прошёл по горизонту и не подошёл близко. Повезло.",
        "Пока вы не спали ночью, на мачте сидела сова.",
        "Вы вернулись после долгого перерыва, а вокруг станции следы. С возвращением.",
        "Ещё тридцать дней на станции. Ледокол привёз почту.",
        "Пять ясных дней подряд, и на закате зелёный луч.",
        "Большая земля подтвердила первое правило вашего наставления.",
    ]),
    ("rare_hints", [
        "Comes after a cyclone has passed.", "A clear morning with a good wind.", "Clear, windy and painless, all at once.",
        "Sometimes in the evening.", "Only by luck.", "If you're awake late at night.", "If you come back after a break.",
        "Every thirty days of the wintering.", "After a long high.", "Keep your first rule in the manual.",
    ], [
        "Приходит, когда циклон уже прошёл.", "Ясное утро с хорошим ветром.", "Ясно, ветрено и ничего не болит, всё сразу.",
        "Иногда по вечерам.", "Только если повезёт.", "Если не спится глубокой ночью.", "Если вернуться после перерыва.",
        "Каждые тридцать дней зимовки.", "После долгого антициклона.", "Подтвердите первое правило в наставлении.",
    ]),
    ("cyclones", ["Agatha", "Boris", "Clara", "Daniel", "Elsa", "Felix", "Greta", "Hugo", "Ida", "Jonas", "Klara", "Leo",
                  "Mira", "Nils", "Olga", "Paul", "Rosa", "Sven", "Tilda", "Ulf", "Vera", "Walter", "Yara", "Zeno"],
                 ["Агата", "Борис", "Вера", "Глеб", "Дарья", "Егор", "Жанна", "Захар", "Ирина", "Кирилл", "Лидия", "Марк",
                  "Нина", "Олег", "Полина", "Роман", "Софья", "Тимур", "Ульяна", "Фёдор", "Эмма", "Юрий", "Яна", "Арсений"]),
    ("pdf_cols", ["Date", "Energy", "Mood", "Clarity", "Pain", "Sleep", "Taken, notes"],
                 ["Дата", "Силы", "Настр.", "Ясность", "Боль", "Сон", "Приборы, заметки"]),
]

UNTRANSLATABLE = ("lang_ru", "lang_en")


def esc(s):
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return s.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')


def write(lang, col):
    out = ['<?xml version="1.0" encoding="utf-8"?>', "<!-- Generated by tools/res.py: edit the table there. -->", "<resources>"]
    for name, en, ru in S:
        if name in UNTRANSLATABLE:
            if col == 1:
                continue
            out.append(f'    <string name="{name}" translatable="false">{esc(en)}</string>')
            continue
        out.append(f'    <string name="{name}">{esc(en if col == 0 else ru)}</string>')
    for name, en, ru in P:
        out.append(f'    <plurals name="{name}">')
        if col == 0:
            out.append(f'        <item quantity="one">{esc(en[0])}</item>')
            out.append(f'        <item quantity="other">{esc(en[1])}</item>')
        else:
            for q, v in zip(("one", "few", "many", "other"), (ru[0], ru[1], ru[2], ru[2])):
                out.append(f'        <item quantity="{q}">{esc(v)}</item>')
        out.append("    </plurals>")
    for name, en, ru in A:
        out.append(f'    <string-array name="{name}">')
        for v in (en if col == 0 else ru):
            out.append(f"        <item>{esc(v)}</item>")
        out.append("    </string-array>")
    out.append("</resources>")
    d = RES / ("values" if lang == "en" else "values-ru")
    d.mkdir(parents=True, exist_ok=True)
    (d / "strings.xml").write_text("\n".join(out) + "\n", encoding="utf-8")


if __name__ == "__main__":
    names = [s[0] for s in S]
    assert len(names) == len(set(names)), [n for n in names if names.count(n) > 1]
    for a in A:
        assert len(a[1]) == len(a[2]), a[0]
    write("en", 0)
    write("ru", 1)
    print(len(S), "strings,", len(P), "plurals,", len(A), "arrays")
