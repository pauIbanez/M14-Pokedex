package com.example.pokedex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.Pokedex
import com.example.pokedex.data.PokedexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CaptureFilter { ALL, CAPTURED, MISSING }

class PokedexViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PokedexRepository(app)

    private val _pokedex = MutableStateFlow<Pokedex?>(null)
    /** Null while the JSON is still being read. */
    val pokedex: StateFlow<Pokedex?> = _pokedex.asStateFlow()

    /** Null until DataStore has answered, so the list never flashes "not captured". */
    val captured: StateFlow<Set<Int>?> = repository.captured
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val query = MutableStateFlow("")
    val filter = MutableStateFlow(CaptureFilter.ALL)

    init {
        viewModelScope.launch { _pokedex.value = repository.loadPokedex() }
    }

    fun setCaptured(id: Int, captured: Boolean) {
        viewModelScope.launch { repository.setCaptured(id, captured) }
    }

    fun toggleCaptured(id: Int) = setCaptured(id, id !in captured.value.orEmpty())
}
