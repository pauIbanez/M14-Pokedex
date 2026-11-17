package com.example.pokedex.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** Root of assets/pokedex.json. */
@Serializable
data class Pokedex(
    val types: List<PokemonType>,
    val pokemon: List<Pokemon>,
) {
    // Lookup maps built after parsing; @Transient keeps them out of the JSON format.
    @Transient private val typesById = types.associateBy { it.id }
    @Transient private val pokemonById = pokemon.associateBy { it.id }

    fun type(id: String): PokemonType? = typesById[id]
    fun byId(id: Int): Pokemon? = pokemonById[id]
}

/** Text available in several languages, chosen by the device language. */
@Serializable
data class LocalizedText(
    val en: String,
    val es: String,
) {
    fun get(language: String): String = if (language == "es") es else en
}

@Serializable
data class PokemonType(
    val id: String,
    val name: LocalizedText,
    /** Hex colour like "#F08030". */
    val color: String,
)

@Serializable
data class Stats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
) {
    val total: Int get() = hp + attack + defense + specialAttack + specialDefense + speed
}

@Serializable
data class Pokemon(
    val id: Int,
    val identifier: String,
    val name: String,
    /** Type ids in slot order, e.g. ["grass", "poison"]. */
    val types: List<String>,
    /** Metres. */
    val height: Double,
    /** Kilograms. */
    val weight: Double,
    val baseExperience: Int,
    val stats: Stats,
    val description: LocalizedText,
    /** Asset path of the sprite, e.g. "sprites/001.png". */
    val sprite: String,
    /** Asset path of the female sprite, only for species that look different. */
    val spriteFemale: String? = null,
) {
    val number: String get() = "#%03d".format(id)
}
