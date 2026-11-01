package app.overwinter.domain

import java.time.Instant
import java.time.ZoneId

/**
 * The observation as a synoptic telegram: five-figure groups, as stations send them.
 *   ТЕЛО 01091   station, day, hour, term
 *   10462        1, wind (two figures), cloud in oktas, visibility
 *   2075/        2, hours slept ×10, or slashes
 *   ГОЛ3 СПН1    pain: where and how strong (or 30000: dry)
 *   40132        4, pressure in tenths of hPa, last four figures
 *   = КОФ ТРН =  the instruments ticked
 */
object Telegram {
    fun groups(o: Obs, callsign: String, zoneCodes: List<String>, gearName: (String) -> String, zone: ZoneId = ZoneId.systemDefault()): List<String> {
        val t = Instant.ofEpochMilli(o.at).atZone(zone)
        val out = ArrayList<String>()
        out += callsign.uppercase().filter { it.isLetterOrDigit() }.take(8).ifEmpty { "ST" }
        out += "%02d%02d%d".format(t.dayOfMonth, t.hour, o.slot.ordinal + 1)
        out += "1%02d%d%d".format(o.wind, o.cloud, o.vis)
        out += o.sleep?.let { "2%03d/".format((it * 10).toInt().coerceIn(0, 999)) } ?: "2////"
        if (o.rain.isEmpty()) out += "30000"
        else for (r in o.rain.sortedBy { it.zone }) out += zoneCodes[r.zone] + r.level
        out += o.hpa?.let { "4%04d".format((it * 10).toInt() % 10000) } ?: "4////"
        val gear = o.gear.map { code(gearName(it)) }.filter { it.isNotEmpty() }
        if (gear.isNotEmpty()) out += "= " + gear.joinToString(" ") + " ="
        return out
    }

    fun code(name: String): String = name.uppercase().filter { it.isLetter() }.take(3)

    private val MORSE = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
        'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
        'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..",
        'А' to ".-", 'Б' to "-...", 'В' to ".--", 'Г' to "--.", 'Д' to "-..", 'Е' to ".", 'Ё' to ".", 'Ж' to "...-",
        'З' to "--..", 'И' to "..", 'Й' to ".---", 'К' to "-.-", 'Л' to ".-..", 'М' to "--", 'Н' to "-.", 'О' to "---",
        'П' to ".--.", 'Р' to ".-.", 'С' to "...", 'Т' to "-", 'У' to "..-", 'Ф' to "..-.", 'Х' to "....", 'Ц' to "-.-.",
        'Ч' to "---.", 'Ш' to "----", 'Щ' to "--.-", 'Ъ' to "--.--", 'Ы' to "-.--", 'Ь' to "-..-", 'Э' to "..-..",
        'Ю' to "..--", 'Я' to ".-.-",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.", '/' to "-..-.", '=' to "-...-",
    )

    /** On/off durations in dot units for [text]: positive is a tone, negative is silence. */
    fun morse(text: String): List<Int> {
        val out = ArrayList<Int>()
        for (ch in text.uppercase()) {
            if (ch == ' ') { out += -7; continue }
            val code = MORSE[ch] ?: continue
            code.forEachIndexed { i, s ->
                if (i > 0) out += -1
                out += if (s == '.') 1 else 3
            }
            out += -3
        }
        return out
    }
}
