package com.example.london_live_bus_journey_tracker.presentation.screens.journey

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
import androidx.compose.material3.HorizontalDivider
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
import com.example.london_live_bus_journey_tracker.domain.model.JourneyOption
import com.example.london_live_bus_journey_tracker.presentation.components.DirectionsHeader
import com.example.london_live_bus_journey_tracker.presentation.components.ErrorState
import com.example.london_live_bus_journey_tracker.presentation.components.JourneyOptionCard
import com.example.london_live_bus_journey_tracker.presentation.components.LoadingState
import com.example.london_live_bus_journey_tracker.presentation.components.MapBottomSheetScaffold
import com.example.london_live_bus_journey_tracker.ui.theme.LightGray
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextPrimary
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary

@Composable
fun JourneyResultsScreen(
    onBackClick: () -> Unit,
    onRouteSelected: (lineId: String, lineName: String, fromName: String, toName: String) -> Unit,
    viewModel: JourneyResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MapBottomSheetScaffold(
        sheetPeekHeight = 380.dp,
        sheetContent = {
            JourneyResultsSheetContent(
                uiState = uiState,
                onRouteSelected = { option ->
                    onRouteSelected(
                        option.lineId,
                        option.lineName,
                        uiState.fromName,
                        uiState.toName
                    )
                },
                onRetry = viewModel::retry
            )
        },
        mapContent = {
            MapPlaceholder()
        }
    )
}

@Composable
private fun JourneyResultsSheetContent(
    uiState: JourneyResultsUiState,
    onRouteSelected: (JourneyOption) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.directions),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(
                horizontal = Spacing.default,
                vertical = Spacing.medium
            )
        )

        DirectionsHeader(
            fromName = uiState.fromName,
            toName = uiState.toName
        )

        Spacer(modifier = Modifier.height(Spacing.default))

        HorizontalDivider(color = LightGray, thickness = 1.dp)

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.extraLarge)
                ) {
                    LoadingState(message = stringResource(R.string.loading))
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.extraLarge)
                ) {
                    ErrorState(
                        message = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
            }
            uiState.journeyOptions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.extraLarge)
                ) {
                    Text(
                        text = stringResource(R.string.no_routes_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                JourneyOptionsList(
                    fromName = uiState.fromName,
                    toName = uiState.toName,
                    options = uiState.journeyOptions,
                    onOptionClick = onRouteSelected
                )
            }
        }
    }
}

@Composable
private fun JourneyOptionsList(
    fromName: String,
    toName: String,
    options: List<JourneyOption>,
    onOptionClick: (JourneyOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$fromName to $toName",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(
                horizontal = Spacing.default,
                vertical = Spacing.medium
            )
        )

        LazyColumn {
            items(options, key = { "${it.lineId}_${it.viaDescription}" }) { option ->
                JourneyOptionCard(
                    routeNumber = option.routeNumber,
                    viaDescription = option.viaDescription,
                    durationMinutes = option.durationMinutes,
                    onClick = { onOptionClick(option) }
                )
            }
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightGray)
    )
}