package com.example.london_live_bus_journey_tracker.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.london_live_bus_journey_tracker.ui.theme.CornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheetScaffold(
    sheetContent: @Composable () -> Unit,
    mapContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = 200.dp
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)  // Bottom padding for safe area
            ) {
                sheetContent()
            }
        },
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RoundedCornerShape(
            topStart = CornerRadius.extraLarge,
            topEnd = CornerRadius.extraLarge
        ),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShadowElevation = 8.dp,
        sheetDragHandle = {
            SheetDragHandle()
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = mapContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.BottomSheetDefaults.DragHandle()
    }
}