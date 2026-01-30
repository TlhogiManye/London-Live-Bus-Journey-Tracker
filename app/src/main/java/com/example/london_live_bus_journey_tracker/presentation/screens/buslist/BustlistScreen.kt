package com.example.london_live_bus_journey_tracker.presentation.screens.buslist

import BusMarker
import StopMarker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.london_live_bus_journey_tracker.R
import com.example.london_live_bus_journey_tracker.domain.model.BusArrival
import com.example.london_live_bus_journey_tracker.presentation.components.BusTrackerMap
import com.example.london_live_bus_journey_tracker.presentation.components.EmptyBusState
import com.example.london_live_bus_journey_tracker.presentation.components.ErrorState
import com.example.london_live_bus_journey_tracker.presentation.components.LoadingState
import com.example.london_live_bus_journey_tracker.presentation.components.MapBottomSheetScaffold
import com.example.london_live_bus_journey_tracker.presentation.components.TimeBadge
import com.example.london_live_bus_journey_tracker.ui.theme.ComponentSize
import com.example.london_live_bus_journey_tracker.ui.theme.CornerRadius
import com.example.london_live_bus_journey_tracker.ui.theme.IconSize
import com.example.london_live_bus_journey_tracker.ui.theme.LightGray
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextPrimary
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary
import com.google.android.gms.maps.model.LatLng

@Composable
fun BusListScreen(
    onBackClick: () -> Unit,
    onBusSelected: (vehicleId: String, naptanId: String, destinationName: String) -> Unit,
    viewModel: BusListViewModel = hiltViewModel()
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
        sheetPeekHeight = 320.dp,
        sheetContent = {
            BusListSheetContent(
                uiState = uiState,
                onBusSelected = { bus ->
                    onBusSelected(bus.vehicleId, bus.naptanId, bus.destinationName)
                },
                onRetry = viewModel::retry
            )
        },
        mapContent = {
            BusListMapContent(
                uiState = uiState
            )
        }
    )
}

@Composable
private fun BusListMapContent(
    uiState: BusListUiState,
    modifier: Modifier = Modifier
) {
    // Create stop markers from route stops (if available)
    val stopMarkers = remember(uiState.routeStops) {
        uiState.routeStops.map { stop ->
            StopMarker(
                id = stop.naptanId,
                name = stop.name,
                position = LatLng(stop.lat, stop.lon),
                isCurrentStop = false
            )
        }
    }

    // Create route path from stops
    val routePath = remember(uiState.routeStops) {
        uiState.routeStops.map { stop ->
            LatLng(stop.lat, stop.lon)
        }
    }

    // Show the first bus as a marker if we have stops
    val primaryBusMarker = remember(uiState.buses, uiState.routeStops) {
        if (uiState.routeStops.isEmpty() || uiState.buses.isEmpty()) {
            null
        } else {
            val firstBus = uiState.buses.first()
            val stop = uiState.routeStops.find { it.naptanId == firstBus.naptanId }
            stop?.let {
                BusMarker(
                    vehicleId = firstBus.vehicleId,
                    position = LatLng(it.lat, it.lon),
                    title = "Bus ${firstBus.lineName} - ${firstBus.displayTime}"
                )
            }
        }
    }

    BusTrackerMap(
        modifier = modifier,
        busMarker = primaryBusMarker,
        stopMarkers = stopMarkers,
        routePath = routePath
    )
}

@Composable
private fun BusListSheetContent(
    uiState: BusListUiState,
    onBusSelected: (BusArrival) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.busses_for),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(
                horizontal = Spacing.default,
                vertical = Spacing.medium
            )
        )

        Text(
            text = stringResource(R.string.route_label, uiState.lineName),
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = Spacing.default)
        )

        Spacer(modifier = Modifier.height(Spacing.default))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.extraLarge)
                ) {
                    LoadingState(message = stringResource(R.string.loading_buses))
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
            uiState.buses.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.extraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyBusState()
                }
            }
            else -> {
                BusArrivalsList(
                    buses = uiState.buses,
                    onBusClick = onBusSelected
                )
            }
        }
    }
}

@Composable
private fun BusArrivalsList(
    buses: List<BusArrival>,
    onBusClick: (BusArrival) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = Spacing.extraLarge)
    ) {
        items(buses, key = { it.vehicleId }) { bus ->
            BusArrivalRow(
                bus = bus,
                onClick = { onBusClick(bus) }
            )
        }
    }
}

@Composable
private fun BusArrivalRow(
    bus: BusArrival,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Spacing.default,
                vertical = Spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(ComponentSize.busIconContainer)
                .clip(RoundedCornerShape(CornerRadius.medium))
                .background(LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bus),
                contentDescription = null,
                modifier = Modifier.size(IconSize.default),
                tint = Color.Unspecified  // Use original icon colors
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.bus_number, bus.vehicleId),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Text(
                text = stringResource(R.string.arriving_at, bus.stationName),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        TimeBadge(minutes = bus.timeToStationMinutes)
    }
}