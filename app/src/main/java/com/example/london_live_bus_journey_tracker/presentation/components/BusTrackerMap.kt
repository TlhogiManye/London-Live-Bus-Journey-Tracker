package com.example.london_live_bus_journey_tracker.presentation.components

import BusMarker
import StopMarker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState


/**
 * Default London center coordinates.
 */
val LONDON_CENTER = LatLng(51.5074, -0.1278)

/**
 * Reusable Google Maps component for bus tracking.
 *
 * @param modifier Modifier for the map container.
 * @param busMarker Optional bus marker to display.
 * @param stopMarkers List of stop markers to display.
 * @param routePath List of coordinates for the route polyline.
 * @param onMapLoaded Callback when map is loaded.
 */
@Composable
fun BusTrackerMap(
    modifier: Modifier = Modifier,
    busMarker: BusMarker? = null,
    stopMarkers: List<StopMarker> = emptyList(),
    routePath: List<LatLng> = emptyList(),
    onMapLoaded: () -> Unit = {}
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            maxZoomPreference = 18f,
            minZoomPreference = 10f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Auto-fit camera to show all markers
    LaunchedEffect(busMarker, stopMarkers) {
        val allPoints = mutableListOf<LatLng>()

        busMarker?.let { allPoints.add(it.position) }
        stopMarkers.forEach { allPoints.add(it.position) }

        if (allPoints.isNotEmpty()) {
            if (allPoints.size == 1) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(allPoints.first(), 15f)
                )
            } else {
                val boundsBuilder = LatLngBounds.Builder()
                allPoints.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLoaded = onMapLoaded
    ) {
        // Draw route polyline
        if (routePath.isNotEmpty()) {
            Polyline(
                points = routePath,
                color = Color(0xFFE53935), // TfL Red
                width = 8f
            )
        }

        // Draw stop markers
        stopMarkers.forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position),
                title = stop.name,
                snippet = if (stop.isCurrentStop) "Current Stop" else null,
                icon = if (stop.isCurrentStop) {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                } else {
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
            )
        }

        // Draw bus marker
        busMarker?.let { bus ->
            Marker(
                state = MarkerState(position = bus.position),
                title = bus.title,
                snippet = "Vehicle: ${bus.vehicleId}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            )
        }
    }
}

/**
 * Simple map showing a single location.
 */
@Composable
fun SimpleLocationMap(
    modifier: Modifier = Modifier,
    center: LatLng = LONDON_CENTER,
    zoom: Float = 14f
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings
    )
}

/**
 * Map showing journey route with origin and destination markers.
 */
@Composable
fun JourneyRouteMap(
    modifier: Modifier = Modifier,
    originPosition: LatLng? = null,
    destinationPosition: LatLng? = null,
    originName: String = "Origin",
    destinationName: String = "Destination"
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LONDON_CENTER, 12f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    // Fit camera to show both points
    LaunchedEffect(originPosition, destinationPosition) {
        val points = listOfNotNull(originPosition, destinationPosition)

        if (points.size == 2) {
            val boundsBuilder = LatLngBounds.Builder()
            points.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        } else if (points.size == 1) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(points.first(), 14f)
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings
    ) {
        originPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = originName,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        destinationPosition?.let {
            Marker(
                state = MarkerState(position = it),
                title = destinationName,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }
    }
}