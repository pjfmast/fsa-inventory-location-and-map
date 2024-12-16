package com.example.inventory.ui.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.data.Item
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


object MapDestination : NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map
}

private val avansHA = LatLng(51.58466, 4.797556)
private val bruutBoulderHal = LatLng(51.5981682, 4.75602)
private val chasseTheater = LatLng(51.5874786, 4.7822118)

/**
 * Entry route for Map screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val mapUiState by viewModel.mapUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            InventoryTopAppBar(
                title = stringResource(MapDestination.titleRes),
                canNavigateBack = true,
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        MapBody(
            itemList = mapUiState.itemList,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun MapBody(
    itemList: List<Item>,
    modifier: Modifier = Modifier
) {

    val boundsBuilder = LatLngBounds.builder()
    if (itemList.isEmpty())
        boundsBuilder
            .include(avansHA)
            .include(bruutBoulderHal)
            .include(chasseTheater)

    itemList.forEach {
        boundsBuilder.include(LatLng(it.latitude, it.longitude))
    }

    val bounds = boundsBuilder.build()

    // To control and observe the map camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bounds.center, 12f)
    }

    val properties by remember { mutableStateOf(MapProperties()) }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            properties = properties,
            cameraPositionState = cameraPositionState
        ) {
            itemList.forEach {
                Marker(
                    state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = "Name: ${it.name}"
                )
            }
            MapEffect(key1 = bounds) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 64))
            }
        }
    }
}