package com.example.london_live_bus_journey_tracker.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Landing : Route

    @Serializable
    data class Search(
        val prefillFrom: String? = null,
        val prefillTo: String? = null
    ) : Route

    @Serializable
    data class JourneyResults(
        val fromId: String,
        val fromName: String,
        val toId: String,
        val toName: String
    ) : Route

    @Serializable
    data class BusList(
        val lineId: String,
        val lineName: String,
        val fromName: String,
        val toName: String
    ) : Route

    @Serializable
    data class Tracking(
        val lineId: String,
        val lineName: String,
        val vehicleId: String,
        val destinationName: String
    ) : Route
}