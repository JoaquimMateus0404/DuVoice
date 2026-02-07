package com.cleansoft.duvoice.ui.home
}
    )
        val sortOrder: SortOrder
        val searchQuery: String?,
        val favoritesOnly: Boolean,
        val category: Category?,
    private data class FilterParams(

    }
        audioPlayer.release()
        super.onCleared()
    override fun onCleared() {

    }
        _currentPlayingId.value = null
        audioPlayer.stop()
    fun stopPlayback() {

    }
        }
            }
                _currentPlayingId.value = recording.id
                audioPlayer.play()
            if (audioPlayer.prepare(recording.filePath)) {
            audioPlayer.release()
            // Parar reprodução atual e começar nova
        } else {
            audioPlayer.togglePlayPause()
        if (_currentPlayingId.value == recording.id) {
        // Se já está a reproduzir a mesma, toggle play/pause
    fun playRecording(recording: Recording) {

    }
        }
            repository.updateCategory(id, category)
        viewModelScope.launch {
    fun updateCategory(id: Long, category: Category) {

    }
        }
            repository.deleteRecording(recording)
            }
                _currentPlayingId.value = null
                audioPlayer.stop()
            if (_currentPlayingId.value == recording.id) {
            // Parar se estiver a reproduzir
        viewModelScope.launch {
    fun deleteRecording(recording: Recording) {

    }
        }
            repository.renameRecording(id, newName)
        viewModelScope.launch {
    fun renameRecording(id: Long, newName: String) {

    }
        }
            repository.toggleFavorite(recording.id, !recording.isFavorite)
        viewModelScope.launch {
    fun toggleFavorite(recording: Recording) {

    }
        _sortOrder.value = order
    fun setSortOrder(order: SortOrder) {

    }
        _searchQuery.value = query?.takeIf { it.isNotBlank() }
    fun setSearchQuery(query: String?) {

    }
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    fun toggleFavoritesFilter() {

    }
        _selectedCategory.value = category
    fun setCategory(category: Category?) {

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
        list.sumOf { it.duration }
    val totalDuration: StateFlow<Long> = recordings.map { list ->

        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalRecordings: StateFlow<Int> = recordings.map { it.size }
    // Estatísticas

    )
        initialValue = emptyList()
        started = SharingStarted.WhileSubscribed(5000),
        scope = viewModelScope,
    }.stateIn(
        )
            sortOrder = params.sortOrder
            searchQuery = params.searchQuery,
            favoritesOnly = params.favoritesOnly,
            category = params.category,
        repository.getFilteredRecordings(
    }.flatMapLatest { params ->
        FilterParams(category, favorites, search, sort)
    ) { category, favorites, search, sort ->
        _sortOrder
        _searchQuery,
        _showFavoritesOnly,
        _selectedCategory,
    val recordings: StateFlow<List<Recording>> = combine(
    @OptIn(ExperimentalCoroutinesApi::class)
    // Combinação de filtros para obter gravações filtradas

    val currentPlayingId: StateFlow<Long?> = _currentPlayingId.asStateFlow()
    private val _currentPlayingId = MutableStateFlow<Long?>(null)
    // Estado da reprodução

    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)

    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()
    private val _searchQuery = MutableStateFlow<String?>(null)

    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()
    private val _showFavoritesOnly = MutableStateFlow(false)

    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    // Filtros

    val audioPlayer = AudioPlayer()
    private val repository = RecordingRepository(application)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.cleansoft.duvoice.util.audio.AudioPlayer
import com.cleansoft.duvoice.data.repository.RecordingRepository
import com.cleansoft.duvoice.data.model.SortOrder
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.model.Category
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import android.app.Application


