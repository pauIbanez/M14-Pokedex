package com.example.pokedex

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokedex.ui.PokemonDetailScreen
import com.example.pokedex.ui.PokemonListScreen
import com.example.pokedex.ui.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Both screens have a coloured header behind the status bar, so use light status bar icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            PokedexTheme {
                PokedexApp()
            }
        }
    }
}

@Composable
fun PokedexApp(viewModel: PokedexViewModel = viewModel()) {
    val pokedex by viewModel.pokedex.collectAsStateWithLifecycle()
    val captured by viewModel.captured.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    // Id of the Pokémon on the detail screen; null shows the list. Survives rotation.
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    // Hoisted so the list keeps its scroll position after coming back from the detail screen.
    val listState = rememberLazyListState()

    BackHandler(enabled = selectedId != null) { selectedId = null }

    val dex = pokedex
    val capturedIds = captured
    if (dex == null || capturedIds == null) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        return
    }

    AnimatedContent(
        targetState = selectedId,
        // Only animate list <-> detail; previous/next inside the detail just updates it.
        contentKey = { it == null },
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
            } else {
                slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
            }
        },
        label = "screen",
    ) { id ->
        val pokemon = id?.let { dex.byId(it) }
        if (pokemon == null) {
            PokemonListScreen(
                pokedex = dex,
                captured = capturedIds,
                query = query,
                onQueryChange = { viewModel.query.value = it },
                filter = filter,
                onFilterChange = { viewModel.filter.value = it },
                listState = listState,
                onOpen = { selectedId = it },
                onToggleCaptured = viewModel::toggleCaptured,
            )
        } else {
            val index = dex.pokemon.indexOf(pokemon)
            PokemonDetailScreen(
                pokemon = pokemon,
                pokedex = dex,
                captured = pokemon.id in capturedIds,
                onBack = { selectedId = null },
                onToggleCaptured = { viewModel.toggleCaptured(pokemon.id) },
                onPrevious = dex.pokemon.getOrNull(index - 1)?.let { prev -> { selectedId = prev.id } },
                onNext = dex.pokemon.getOrNull(index + 1)?.let { next -> { selectedId = next.id } },
            )
        }
    }
}
