# Changelog

<!--
Telegram: end each version's section with a hidden block for the StillCraft chat. Start it with a
line "<!-- tg", then 2-4 short lines in Russian for users (what is new, what is fixed), no markdown,
then close it like any HTML comment. The release notes carry it, and the chat bot posts those lines.
-->

## [1.1.0] - 2026-11-20

### Added
- Blood pressure: an observation can carry a cuff reading (systolic, diastolic and the pulse if the cuff gave it). It shows in the day's log, and the doctor's PDF gives the average with the range.
- Sleep as lights-out and reveille: instead of hours, set when you went to bed and got up, a quarter of an hour at a time; the hours are worked out, and the way you chose is kept for the next morning. The doctor's PDF gives the usual bedtime and waking time.

<!-- tg
Теперь можно записывать давление с тонометра: верхнее, нижнее и пульс. Оно видно в журнале и в отчёте для врача
Сон можно отмечать временем отбоя и подъёма, а не только часами
-->

## [1.0.1] - 2026-11-16

### Fixed
- A sudden power-off (a flat battery, a frozen phone) could leave the station journal or the barograph tape empty. Both are now forced to the disk before they replace the old files, the previous version is kept beside each as a spare, and a damaged file falls back to that spare instead of an empty journal.

<!-- tg
Поправили сохранение: если телефон внезапно выключится (сел аккумулятор, завис), журнал станции больше не может оказаться пустым.
Записи сохраняются на диск надёжно, а рядом хранится прошлая версия на всякий случай.
-->

## [1.0.0] - 2026-11-01

### Added
- Three synoptic terms a day with reminders: wind (energy, Beaufort), cloud (mood, oktas), visibility, pain as precipitation on a body map, sleep, instruments and a watch journal.
- The report as a five-figure telegram on paper tape, with Morse from the telegraph key.
- The station drawn in code that grows with the terms kept, ranks, ten rare phenomena, snow drifts after missed days, 90-day shifts.
- The barogram: thirty days, named cyclones and highs, the pressure from the phone's barometer.
- The observer's manual: patterns the station finds (instruments, short nights, falling pressure), kept or struck out.
- Evening forecasts for tomorrow, mine and the station's, and how often they come true.
- A plain PDF report for a doctor.
- Polar day and polar night themes, English and Russian, no internet permission.

<!-- tg
Дневник самочувствия, который ведут как полярную станцию: силы — это ветер, настроение — облачность, боль — осадки на карте тела
Три срока в день по десять секунд, сводка уходит телеграммой с морзянкой
Плохие полосы получают имена, как циклоны, а станция сама замечает, что на вас влияет: кофе, короткий сон, падение давления
Для врача есть обычный PDF со всеми днями
-->
