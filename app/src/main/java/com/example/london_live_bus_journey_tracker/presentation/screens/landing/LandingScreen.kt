package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.london_live_bus_journey_tracker.R
import com.example.london_live_bus_journey_tracker.domain.model.RecentSearch
import com.example.london_live_bus_journey_tracker.presentation.components.LandingSearchBar
import com.example.london_live_bus_journey_tracker.presentation.components.MapBottomSheetScaffold
import com.example.london_live_bus_journey_tracker.presentation.components.RouteHistoryCard
import com.example.london_live_bus_journey_tracker.presentation.components.SimpleLocationMap
import com.example.london_live_bus_journey_tracker.presentation.components.StopMarker
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

@Composable
fun LandingScreen(
    onSearchClick: () -> Unit,
    onRecentSearchClick: (RecentSearch) -> Unit,
    viewModel: LandingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Convert bus stops to StopMarker for map display
    val busStopMarkers = remember(uiState.busStops) {
        uiState.busStops.map { stop ->
            StopMarker(
                id = stop.id,
                name = stop.name,
                position = LatLng(stop.lat, stop.lon)
            )
        }
    }

    MapBottomSheetScaffold(
        sheetPeekHeight = 280.dp,
        sheetContent = {
            LandingSheetContent(
                recentSearches = uiState.recentSearches,
                onSearchClick = onSearchClick,
                onRecentSearchClick = onRecentSearchClick
            )
        },
        mapContent = {
            // Show London map with bus stops
            SimpleLocationMap(busStops = busStopMarkers)
        }
    )
}

@Composable
private fun LandingSheetContent(
    recentSearches: List<RecentSearch>,
    onSearchClick: () -> Unit,
    onRecentSearchClick: (RecentSearch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        LandingSearchBar(
            onClick = onSearchClick,
            modifier = Modifier.padding(horizontal = Spacing.default)
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        if (recentSearches.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = Spacing.default)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            LazyColumn(
                contentPadding = PaddingValues(bottom = Spacing.extraLarge)
            ) {
                items(recentSearches, key = { it.id }) { search ->
                    RouteHistoryCard(
                        routeNumber = search.routeNumber,
                        viaDescription = search.viaDescription,
                        durationMinutes = search.durationMinutes,
                        onClick = { onRecentSearchClick(search) }
                    )
                }
            }
        }
    }
}