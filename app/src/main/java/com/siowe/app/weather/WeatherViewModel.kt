package com.siowe.app.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherState {
    data object Loading : WeatherState
    data class Ready(val weather: Weather) : WeatherState
    data class Error(val message: String) : WeatherState
}

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            if (_state.value !is WeatherState.Ready) _state.value = WeatherState.Loading
            _state.value = try {
                WeatherState.Ready(WeatherRepo.load())
            } catch (e: Exception) {
                WeatherState.Error(e.message ?: "Нет соединения")
            }
        }
    }
}
