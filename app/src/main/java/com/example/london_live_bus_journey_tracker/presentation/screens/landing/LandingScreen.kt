package com.example.london_live_bus_journey_tracker.presentation.screens.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.london_live_bus_journey_tracker.ui.theme.LightGray
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary

@Composable
fun LandingScreen(
    onSearchClick: () -> Unit,
    onRecentSearchClick: (RecentSearch) -> Unit,
    viewModel: LandingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            MapPlaceholder()
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

            LazyColumn {
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

@Composable
private fun MapPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        // Google Maps will be integrated here
        // For now just show a placeholder background
    }
}