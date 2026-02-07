package com.cleansoft.duvoice.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansoft.duvoice.data.model.RecordingStats
import com.cleansoft.duvoice.data.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)

    private val _stats = MutableStateFlow(RecordingStats())
    val stats: StateFlow<RecordingStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _stats.value = repository.getStats()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadStats()
    }
}

