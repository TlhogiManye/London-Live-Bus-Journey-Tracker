package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import LandingUiState
import androidx.lifecycle.ViewModel
import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject



@HiltViewModel
class LandingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        // Mock data matching Figma design
        val mockSearches = listOf(
            RecentSearch(
                id = "1",
                fromName = "Victoria",
                toName = "Oxford St",
                routeNumber = "24",
                viaDescription = "Bus via Pimlico",
                durationMinutes = 25
            ),
            RecentSearch(
                id = "2",
                fromName = "Paddington",
                toName = "Liverpool St",
                routeNumber = "38",
                viaDescription = "Bus via Notting Hill",
                durationMinutes = 30
            )
        )

        _uiState.update { state ->
            state.copy(recentSearches = mockSearches)
        }
    }

    fun addRecentSearch(search: RecentSearch) {
        _uiState.update { state ->
            val updated = listOf(search) + state.recentSearches.filter { it.id != search.id }
            state.copy(recentSearches = updated.take(10))
        }
    }
}