package com.example.pokedex.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.pokedex.CaptureFilter
import com.example.pokedex.R
import com.example.pokedex.data.Pokedex
import com.example.pokedex.data.Pokemon

@Composable
fun PokemonListScreen(
    pokedex: Pokedex,
    captured: Set<Int>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: CaptureFilter,
    onFilterChange: (CaptureFilter) -> Unit,
    listState: LazyListState,
    onOpen: (Int) -> Unit,
    onToggleCaptured: (Int) -> Unit,
) {
    val visible = remember(pokedex, captured, query, filter) {
        filterPokemon(pokedex.pokemon, captured, query, filter)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Header(
            capturedCount = captured.size,
            total = pokedex.pokemon.size,
            query = query,
            onQueryChange = onQueryChange,
        )
        FilterRow(filter, onFilterChange)

        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    stringResource(R.string.no_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }

        val navBar = WindowInsets.navigationBars.asPaddingValues()
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp, top = 4.dp,
                bottom = 12.dp + navBar.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visible, key = { it.id }) { pokemon ->
                PokemonRow(
                    pokemon = pokemon,
                    pokedex = pokedex,
                    captured = pokemon.id in captured,
                    onClick = { onOpen(pokemon.id) },
                    onToggleCaptured = { onToggleCaptured(pokemon.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private fun filterPokemon(
    all: List<Pokemon>,
    captured: Set<Int>,
    query: String,
    filter: CaptureFilter,
): List<Pokemon> {
    val q = query.trim().lowercase().removePrefix("#")
    val isNumber = q.isNotEmpty() && q.all { it.isDigit() }
    return all.filter { p ->
        val matchesFilter = when (filter) {
            CaptureFilter.ALL -> true
            CaptureFilter.CAPTURED -> p.id in captured
            CaptureFilter.MISSING -> p.id !in captured
        }
        val matchesQuery = q.isEmpty() ||
            p.name.lowercase().contains(q) ||
            p.identifier.contains(q) ||
            // "25" finds #025, "1" finds #001, #010-#019 and #100-#151
            (isNumber && (p.id.toString().startsWith(q) || "%03d".format(p.id).startsWith(q)))
        matchesFilter && matchesQuery
    }
}

@Composable
private fun Header(capturedCount: Int, total: Int, query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PokeballIndicator(captured = true, size = 30.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = onPrimary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.captured_count, capturedCount, total),
                style = MaterialTheme.typography.labelLarge,
                color = onPrimary,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else capturedCount / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = onPrimary.copy(alpha = 0.25f),
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(16.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Close, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun FilterRow(filter: CaptureFilter, onFilterChange: (CaptureFilter) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val labels = mapOf(
            CaptureFilter.ALL to R.string.filter_all,
            CaptureFilter.CAPTURED to R.string.filter_captured,
            CaptureFilter.MISSING to R.string.filter_missing,
        )
        labels.forEach { (value, label) ->
            FilterChip(
                selected = filter == value,
                onClick = { onFilterChange(value) },
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: Pokemon,
    pokedex: Pokedex,
    captured: Boolean,
    onClick: () -> Unit,
    onToggleCaptured: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainColor = pokedex.type(pokemon.types.first())?.composeColor() ?: MaterialTheme.colorScheme.primary
    val container by animateColorAsState(
        if (captured) mainColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainer,
        label = "rowColor",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = container,
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(mainColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                PokemonSprite(pokemon.sprite, contentDescription = null, modifier = Modifier.size(60.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    pokemon.number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    pokemon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.mapNotNull { pokedex.type(it) }.forEach { TypeChip(it) }
                }
            }
            val description = stringResource(if (captured) R.string.captured else R.string.not_captured)
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = description, onClick = onToggleCaptured),
                contentAlignment = Alignment.Center,
            ) {
                PokeballIndicator(captured)
            }
        }
    }
}
