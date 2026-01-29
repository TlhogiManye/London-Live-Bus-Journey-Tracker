package com.example.london_live_bus_journey_tracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.london_live_bus_journey_tracker.R
import com.example.london_live_bus_journey_tracker.ui.theme.ComponentSize
import com.example.london_live_bus_journey_tracker.ui.theme.CornerRadius
import com.example.london_live_bus_journey_tracker.ui.theme.EndRed
import com.example.london_live_bus_journey_tracker.ui.theme.IconSize
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.StartGreen
import com.example.london_live_bus_journey_tracker.ui.theme.TextPrimary
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary
import com.example.london_live_bus_journey_tracker.ui.theme.White

@Composable
fun DirectionsHeader(
    fromName: String,
    toName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.default)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Green "From" marker - using complete marker icon or outline circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, StartGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location_pin),
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.medium),
                    tint = StartGreen
                )
            }

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column {
                Text(
                    text = fromName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "From",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.medium))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red "To" marker - using complete marker icon or outline circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, EndRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flag_small),
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.medium),
                    tint = EndRed
                )
            }

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column {
                Text(
                    text = toName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "To",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun JourneyOptionCard(
    routeNumber: String,
    viaDescription: String,
    durationMinutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_route),
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.default),
                    tint = Color.Unspecified  // Use original icon colors
                )
            }

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Route $routeNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = viaDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(Spacing.small))

            TimeBadge(minutes = durationMinutes)
        }
    }
}