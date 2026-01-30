package com.example.london_live_bus_journey_tracker.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.london_live_bus_journey_tracker.R
import com.example.london_live_bus_journey_tracker.domain.model.Location
import com.example.london_live_bus_journey_tracker.ui.theme.BusYellow
import com.example.london_live_bus_journey_tracker.ui.theme.ComponentSize
import com.example.london_live_bus_journey_tracker.ui.theme.CornerRadius
import com.example.london_live_bus_journey_tracker.ui.theme.IconSize
import com.example.london_live_bus_journey_tracker.ui.theme.MediumGray
import com.example.london_live_bus_journey_tracker.ui.theme.Spacing
import com.example.london_live_bus_journey_tracker.ui.theme.TextOnYellow
import com.example.london_live_bus_journey_tracker.ui.theme.TextPrimary
import com.example.london_live_bus_journey_tracker.ui.theme.TextSecondary
import com.example.london_live_bus_journey_tracker.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onSearchComplete: (fromId: String, fromName: String, toId: String, toName: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val fromFocusRequester = remember { FocusRequester() }
    val toFocusRequester = remember { FocusRequester() }

    // Auto-focus the from field on launch
    LaunchedEffect(Unit) {
        fromFocusRequester.requestFocus()
    }

    // Move focus to "To" field when "From" is selected
    LaunchedEffect(uiState.activeField) {
        when (uiState.activeField) {
            ActiveField.TO -> toFocusRequester.requestFocus()
            else -> { }
        }
    }

    // Check if search can be performed
    val canSearch = uiState.fromLocation != null && uiState.toLocation != null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = ComponentSize.buttonHeight + Spacing.extraLarge + Spacing.default)
            ) {
                // Header with back button
                SearchHeader(
                    onBackClick = {
                        keyboardController?.hide()
                        onBackClick()
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Search input fields
                Column(modifier = Modifier.padding(horizontal = Spacing.default)) {
                    FloatingLabelTextField(
                        value = uiState.fromText,
                        onValueChange = viewModel::onFromTextChanged,
                        label = stringResource(R.string.label_from),
                        isFocused = uiState.activeField == ActiveField.FROM,
                        onFocusChange = { focused ->
                            if (focused) viewModel.onFromFocused()
                        },
                        onClear = if (uiState.fromText.isNotEmpty()) viewModel::onClearFrom else null,
                        focusRequester = fromFocusRequester,
                        imeAction = ImeAction.Next,
                        onNext = { toFocusRequester.requestFocus() }
                    )

                    Spacer(modifier = Modifier.height(Spacing.default))

                    FloatingLabelTextField(
                        value = uiState.toText,
                        onValueChange = viewModel::onToTextChanged,
                        label = stringResource(R.string.label_to),
                        isFocused = uiState.activeField == ActiveField.TO,
                        onFocusChange = { focused ->
                            if (focused) viewModel.onToFocused()
                        },
                        onClear = if (uiState.toText.isNotEmpty()) viewModel::onClearTo else null,
                        focusRequester = toFocusRequester,
                        imeAction = ImeAction.Done,
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                // Show loading indicator when searching
                if (uiState.isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.default),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BusYellow,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Suggestions or Recent Searches
                val showSuggestions = uiState.suggestions.isNotEmpty()
                val showRecentSearches = !showSuggestions &&
                        uiState.fromText.isEmpty() &&
                        uiState.toText.isEmpty() &&
                        uiState.recentSearches.isNotEmpty()

                when {
                    showSuggestions -> {
                        SuggestionsSection(
                            suggestions = uiState.suggestions,
                            onSuggestionClick = viewModel::onSuggestionSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    showRecentSearches -> {
                        RecentSearchesSection(
                            recentSearches = uiState.recentSearches,
                            onRecentSearchClick = viewModel::onRecentSearchSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Search Button at the bottom
            Button(
                onClick = {
                    keyboardController?.hide()
                    val from = uiState.fromLocation
                    val to = uiState.toLocation
                    if (from != null && to != null) {
                        onSearchComplete(
                            from.effectiveJourneyId,
                            from.name,
                            to.effectiveJourneyId,
                            to.name
                        )
                    }
                },
                enabled = canSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = Spacing.default)
                    .padding(bottom = Spacing.extraLarge)
                    .imePadding()
                    .height(ComponentSize.buttonHeight),
                shape = RoundedCornerShape(CornerRadius.medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BusYellow,
                    contentColor = TextOnYellow,
                    disabledContainerColor = MediumGray,
                    disabledContentColor = TextTertiary
                )
            ) {
                Text(
                    text = stringResource(R.string.search_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.small,
                end = Spacing.default,
                top = Spacing.extraLarge + Spacing.medium,
                bottom = Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(R.string.content_description_back),
                tint = TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(Spacing.small))

        Text(
            text = stringResource(R.string.search_address_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Done,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    val borderColor = if (isFocused) BusYellow else MediumGray
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val labelColor = if (isFocused) BusYellow else TextTertiary

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentSize.inputFieldHeight)
                .clip(RoundedCornerShape(CornerRadius.medium))
                .border(borderWidth, borderColor, RoundedCornerShape(CornerRadius.medium)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.default),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { state ->
                                onFocusChange(state.isFocused)
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = TextPrimary
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BusYellow),
                        keyboardOptions = KeyboardOptions(imeAction = imeAction),
                        keyboardActions = KeyboardActions(
                            onNext = { onNext?.invoke() },
                            onDone = { onDone?.invoke() }
                        )
                    )
                }

                if (value.isNotEmpty() && onClear != null) {
                    Spacer(modifier = Modifier.width(Spacing.small))
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(IconSize.default)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.content_description_clear),
                            tint = TextTertiary
                        )
                    }
                }
            }
        }

        // Floating label
        if (isFocused || value.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier
                    .padding(start = Spacing.medium)
                    .align(Alignment.TopStart)
                    .offset(y = (-8).dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextTertiary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = Spacing.default)
            )
        }
    }
}

@Composable
private fun SuggestionsSection(
    suggestions: List<Location>,
    onSuggestionClick: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.suggestions_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = Spacing.default)
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        LazyColumn {
            // Use itemsIndexed with index as part of key to ensure uniqueness
            itemsIndexed(
                items = suggestions,
                key = { index, location -> "${location.id}_$index" }
            ) { _, location ->
                SuggestionItem(
                    location = location,
                    onClick = { onSuggestionClick(location) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    location: Location,
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
        Icon(
            painter = painterResource(id = R.drawable.ic_location_pin),
            contentDescription = null,
            modifier = Modifier.size(IconSize.default),
            tint = BusYellow
        )

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            if (location.address.isNotEmpty()) {
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = stringResource(R.string.location_united_kingdom),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<RecentJourneySearch>,
    onRecentSearchClick: (RecentJourneySearch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.recent_searches_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = Spacing.default)
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        LazyColumn {
            itemsIndexed(
                items = recentSearches,
                key = { index, search -> "${search.displayText}_$index" }
            ) { _, search ->
                RecentSearchItem(
                    recentSearch = search,
                    onClick = { onRecentSearchClick(search) }
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    recentSearch: RecentJourneySearch,
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
        Icon(
            painter = painterResource(id = R.drawable.ic_location_pin),
            contentDescription = null,
            modifier = Modifier.size(IconSize.default),
            tint = TextSecondary
        )

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recentSearch.displayText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Text(
                text = stringResource(R.string.location_united_kingdom),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}