package com.example.london_live_bus_journey_tracker.presentation.screens.tracking

import BusMarker
import StopMarker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.london_live_bus_journey_tracker.R
import com.example.london_live_bus_journey_tracker.presentation.components.BusTrackerMap
import com.example.london_live_bus_journey_tracker.presentation.components.ErrorState
import com.example.london_live_bus_journey_tracker.presentation.components.LoadingState
import com.example.london_live_bus_journey_tracker.presentation.components.MapBottomSheetScaffold
import com.example.london_live_bus_journey_tracker.ui.theme.BusYellow
import com.example.london_live_bus_journey_tracker.ui.theme.ComponentSize
import com.example.london_live_bus_journey_tracker.ui.theme.CornerRadius
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextOnYellow
import com.example.london_live_bus_journey_tracker.ui.theme.TextPrimary
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

@Composable
fun TrackingScreen(
    onBackClick: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.stopPolling()
                else -> { }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MapBottomSheetScaffold(
        sheetPeekHeight = 140.dp,
        sheetContent = {
            TrackingSheetContent(
                uiState = uiState,
                onRetry = viewModel::retry
            )
        },
        mapContent = {
            TrackingMapContent(uiState = uiState)
        }
    )
}

@Composable
private fun TrackingMapContent(
    uiState: TrackingUiState,
    modifier: Modifier = Modifier
) {
    // Convert UI state to map markers
    val busMarker = remember(uiState.busPosition, uiState.vehicleId, uiState.lineName) {
        uiState.busPosition?.let { pos ->
            BusMarker(
                vehicleId = uiState.vehicleId,
                position = LatLng(pos.lat, pos.lon),
                title = "Bus ${uiState.lineName}"
            )
        }
    }

    val stopMarkers = remember(uiState.routeStops) {
        uiState.routeStops.map { stop ->
            StopMarker(
                id = stop.naptanId,
                name = stop.name,
                position = LatLng(stop.lat, stop.lon),
                isCurrentStop = stop.isCurrentStop
            )
        }
    }

    val routePath = remember(uiState.routeStops) {
        uiState.routeStops.map { stop ->
            LatLng(stop.lat, stop.lon)
        }
    }

    BusTrackerMap(
        modifier = modifier,
        busMarker = busMarker,
        stopMarkers = stopMarkers,
        routePath = routePath
    )
}

@Composable
private fun TrackingSheetContent(
    uiState: TrackingUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.extraLarge)
    ) {
        Text(
            text = stringResource(R.string.trip),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(
                horizontal = Spacing.default,
                vertical = Spacing.medium
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large)
                ) {
                    LoadingState(message = stringResource(R.string.loading))
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large)
                ) {
                    ErrorState(
                        message = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
            }
            else -> {
                TripInfoCard(
                    nextStopName = uiState.nextStopName,
                    timeToNextStop = uiState.timeToNextStop,
                    modifier = Modifier.padding(horizontal = Spacing.default)
                )
            }
        }
    }
}

@Composable
private fun TripInfoCard(
    nextStopName: String,
    timeToNextStop: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.medium))
            .background(BusYellow)
            .padding(Spacing.default),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.next_stop, nextStopName),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextOnYellow
            )

            Text(
                text = stringResource(R.string.location_description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnYellow.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        TripTimeBadge(minutes = timeToNextStop)
    }
}

@Composable
private fun TripTimeBadge(
    minutes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(ComponentSize.timeBadgeWidth)
            .height(ComponentSize.timeBadgeHeight)
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$minutes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.minutes_short),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}