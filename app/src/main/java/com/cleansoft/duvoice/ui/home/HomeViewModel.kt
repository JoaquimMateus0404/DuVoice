package com.cleansoft.duvoice.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.model.SortOrder
import com.cleansoft.duvoice.data.repository.RecordingRepository
import com.cleansoft.duvoice.util.audio.AudioPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)
    val audioPlayer = AudioPlayer()

    // Filtros
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Estado da reprodução
    private val _currentPlayingId = MutableStateFlow<Long?>(null)
    val currentPlayingId: StateFlow<Long?> = _currentPlayingId.asStateFlow()

    // Combinação de filtros para obter gravações filtradas
    @OptIn(ExperimentalCoroutinesApi::class)
    val recordings: StateFlow<List<Recording>> = combine(
        _selectedCategory,
        _showFavoritesOnly,
        _searchQuery,
        _sortOrder
    ) { category, favorites, search, sort ->
        FilterParams(category, favorites, search, sort)
    }.flatMapLatest { params ->
        repository.getFilteredRecordings(
            category = params.category,
            favoritesOnly = params.favoritesOnly,
            searchQuery = params.searchQuery,
            sortOrder = params.sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estatísticas
    val totalRecordings: StateFlow<Int> = recordings.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDuration: StateFlow<Long> = recordings.map { list ->
        list.sumOf { it.duration }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query?.takeIf { it.isNotBlank() }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleFavorite(recording: Recording) {
        viewModelScope.launch {
            repository.toggleFavorite(recording.id, !recording.isFavorite)
        }
    }

    fun renameRecording(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameRecording(id, newName)
        }
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            // Parar se estiver a reproduzir
            if (_currentPlayingId.value == recording.id) {
                audioPlayer.stop()
                _currentPlayingId.value = null
            }
            repository.deleteRecording(recording)
        }
    }

    fun updateCategory(id: Long, category: Category) {
        viewModelScope.launch {
            repository.updateCategory(id, category)
        }
    }

    fun playRecording(recording: Recording) {
        // Se já está a reproduzir a mesma, toggle play/pause
        if (_currentPlayingId.value == recording.id) {
            audioPlayer.togglePlayPause()
        } else {
            // Parar reprodução atual e começar nova
            audioPlayer.release()
            if (audioPlayer.prepare(recording.filePath)) {
                audioPlayer.play()
                _currentPlayingId.value = recording.id
            }
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _currentPlayingId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }

    private data class FilterParams(
        val category: Category?,
        val favoritesOnly: Boolean,
        val searchQuery: String?,
        val sortOrder: SortOrder
    )
}
