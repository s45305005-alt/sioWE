package com.siowe.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clerk.ui.userbutton.UserButton
import com.siowe.app.weather.DayForecast
import com.siowe.app.weather.HourForecast
import com.siowe.app.weather.Weather
import com.siowe.app.weather.WeatherState
import com.siowe.app.weather.WeatherViewModel
import com.siowe.app.weather.describe
import com.siowe.app.weather.emoji

private val Soft = Color.White.copy(alpha = 0.72f)

@Composable
fun WeatherScreen(vm: WeatherViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ready = state as? WeatherState.Ready
    SkyBackground(code = ready?.weather?.code ?: 1, isDay = ready?.weather?.isDay ?: false) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Header(onRefresh = vm::refresh)
            when (val s = state) {
                is WeatherState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                is WeatherState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        Modifier.padding(24.dp).glass().clickable { vm.refresh() }.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Не удалось загрузить погоду", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Проверьте интернет и нажмите, чтобы повторить", color = Soft, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                }
                is WeatherState.Ready -> Content(s.weather)
            }
        }
    }
}

@Composable
private fun Header(onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("sioWE", color = Soft, fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
            Text("Киев", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            Modifier.size(44.dp).glass(RoundedCornerShape(22.dp)).clickable(onClick = onRefresh),
            contentAlignment = Alignment.Center,
        ) { Text("⟳", color = Color.White, fontSize = 20.sp) }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { UserButton() }
    }
}

@Composable
private fun Content(w: Weather) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Hero
        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji(w.code, w.isDay), fontSize = 64.sp)
            Text("${w.temp}°", color = Color.White, fontSize = 104.sp, fontWeight = FontWeight.Thin)
            Text(describe(w.code), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            val today = w.daily.first()
            Text("Макс. ${today.max}°  ·  Мин. ${today.min}°", color = Soft, fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp))
        }

        // Hourly
        GlassCard {
            Label("ПОЧАСОВОЙ ПРОГНОЗ")
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) { items(w.hourly) { HourItem(it) } }
        }

        // Details
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Detail("ОЩУЩАЕТСЯ", "${w.feels}°", "Как на самом деле", Modifier.weight(1f))
            Detail("ВЕТЕР", "${w.windMs} м/с", "На высоте 10 м", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Detail("ВЛАЖНОСТЬ", "${w.humidity}%", "Относительная", Modifier.weight(1f))
            Detail("ДАВЛЕНИЕ", "${w.pressureMmHg}", "мм рт. ст.", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Detail("ВОСХОД", w.sunrise, "🌅", Modifier.weight(1f))
            Detail("ЗАКАТ", w.sunset, "🌇", Modifier.weight(1f))
        }

        // 7 days
        val lo = w.daily.minOf { it.min }
        val hi = w.daily.maxOf { it.max }
        GlassCard {
            Label("ПРОГНОЗ НА 7 ДНЕЙ")
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                w.daily.forEach { DayRow(it, lo, hi) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().glass()) { content() }
}

@Composable
private fun Label(text: String) {
    Text(text, color = Soft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 18.dp, top = 14.dp))
}

@Composable
private fun HourItem(h: HourForecast) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(h.label, color = Soft, fontSize = 13.sp)
        Text(emoji(h.code, h.isDay), fontSize = 24.sp)
        Text("${h.temp}°", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Detail(title: String, value: String, note: String, modifier: Modifier) {
    Column(modifier.glass().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = Soft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Text(note, color = Soft, fontSize = 13.sp)
    }
}

@Composable
private fun DayRow(d: DayForecast, lo: Int, hi: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(d.label, color = Color.White, fontSize = 17.sp, modifier = Modifier.width(84.dp))
        Text(emoji(d.code, true), fontSize = 22.sp, modifier = Modifier.width(40.dp))
        Text("${d.min}°", color = Soft, fontSize = 16.sp, modifier = Modifier.width(38.dp), textAlign = TextAlign.End)
        RangeBar(d.min, d.max, lo, hi, Modifier.weight(1f).padding(horizontal = 10.dp))
        Text("${d.max}°", color = Color.White, fontSize = 16.sp, modifier = Modifier.width(34.dp))
    }
}

@Composable
private fun RangeBar(min: Int, max: Int, lo: Int, hi: Int, modifier: Modifier) {
    val span = (hi - lo).coerceAtLeast(1).toFloat()
    val start = (min - lo) / span
    val end = (max - lo) / span
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier.height(6.dp).background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(3.dp))
    ) {
        val w: Dp = maxWidth
        Box(
            Modifier.offset(x = w * start).width((w * (end - start)).coerceAtLeast(6.dp)).fillMaxHeight()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF7FD6FF), Color(0xFFFFC95E))),
                    RoundedCornerShape(3.dp),
                )
        )
    }
}
