package com.siowe.app.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class HourForecast(val label: String, val temp: Int, val code: Int, val isDay: Boolean)
data class DayForecast(val label: String, val min: Int, val max: Int, val code: Int)

data class Weather(
    val temp: Int,
    val feels: Int,
    val humidity: Int,
    val windMs: Int,
    val pressureMmHg: Int,
    val code: Int,
    val isDay: Boolean,
    val sunrise: String,
    val sunset: String,
    val hourly: List<HourForecast>,
    val daily: List<DayForecast>,
)

/** Open-Meteo: бесплатный API без ключа и регистрации. Киев: 50.4501, 30.5234. */
object WeatherRepo {
    private const val URL_STR =
        "https://api.open-meteo.com/v1/forecast?latitude=50.4501&longitude=30.5234" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,pressure_msl,is_day" +
            "&hourly=temperature_2m,weather_code,is_day" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset" +
            "&wind_speed_unit=ms&timezone=Europe%2FKyiv&forecast_days=7"

    suspend fun load(): Weather = withContext(Dispatchers.IO) {
        val conn = URL(URL_STR).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            parse(JSONObject(body))
        } finally {
            conn.disconnect()
        }
    }

    private fun JSONArray.strings() = List(length()) { getString(it) }
    private fun JSONArray.doubles() = List(length()) { getDouble(it) }
    private fun JSONArray.ints() = List(length()) { getInt(it) }

    private fun parse(root: JSONObject): Weather {
        val cur = root.getJSONObject("current")
        val h = root.getJSONObject("hourly")
        val d = root.getJSONObject("daily")

        val hTimes = h.getJSONArray("time").strings()
        val hTemps = h.getJSONArray("temperature_2m").doubles()
        val hCodes = h.getJSONArray("weather_code").ints()
        val hDay = h.getJSONArray("is_day").ints()
        val nowHour = cur.getString("time").substring(0, 13)
        val start = hTimes.indexOfFirst { it.substring(0, 13) >= nowHour }.coerceAtLeast(0)
        val hourly = (start until minOf(start + 24, hTimes.size)).map { i ->
            HourForecast(
                label = if (i == start) "Сейчас" else hTimes[i].substring(11, 16),
                temp = hTemps[i].roundToInt(),
                code = hCodes[i],
                isDay = hDay[i] == 1,
            )
        }

        val dDates = d.getJSONArray("time").strings()
        val dMax = d.getJSONArray("temperature_2m_max").doubles()
        val dMin = d.getJSONArray("temperature_2m_min").doubles()
        val dCode = d.getJSONArray("weather_code").ints()
        val daily = dDates.indices.map { i ->
            val name = if (i == 0) "Сегодня" else LocalDate.parse(dDates[i])
                .dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
            DayForecast(name, dMin[i].roundToInt(), dMax[i].roundToInt(), dCode[i])
        }

        return Weather(
            temp = cur.getDouble("temperature_2m").roundToInt(),
            feels = cur.getDouble("apparent_temperature").roundToInt(),
            humidity = cur.getInt("relative_humidity_2m"),
            windMs = cur.getDouble("wind_speed_10m").roundToInt(),
            pressureMmHg = (cur.getDouble("pressure_msl") * 0.750062).roundToInt(),
            code = cur.getInt("weather_code"),
            isDay = cur.getInt("is_day") == 1,
            sunrise = d.getJSONArray("sunrise").getString(0).substring(11, 16),
            sunset = d.getJSONArray("sunset").getString(0).substring(11, 16),
            hourly = hourly,
            daily = daily,
        )
    }
}

fun describe(code: Int): String = when (code) {
    0 -> "Ясно"
    1 -> "Преимущественно ясно"
    2 -> "Переменная облачность"
    3 -> "Пасмурно"
    45, 48 -> "Туман"
    51, 53, 55, 56, 57 -> "Морось"
    61, 63, 65, 66, 67 -> "Дождь"
    71, 73, 75, 77 -> "Снег"
    80, 81, 82 -> "Ливень"
    85, 86 -> "Снегопад"
    95, 96, 99 -> "Гроза"
    else -> "—"
}

fun emoji(code: Int, isDay: Boolean): String = when (code) {
    0 -> if (isDay) "☀️" else "🌙"
    1 -> if (isDay) "🌤️" else "🌙"
    2 -> if (isDay) "⛅" else "☁️"
    3 -> "☁️"
    45, 48 -> "🌫️"
    51, 53, 55, 56, 57 -> "🌦️"
    61, 63, 65, 66, 67 -> "🌧️"
    71, 73, 75, 77, 85, 86 -> "❄️"
    80, 81, 82 -> "🌧️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}
