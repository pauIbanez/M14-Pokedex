"""
Converts the raw Pokémon CSV + PNG data into the app's single JSON file.

Usage:
    python tools/build_pokedex_json.py "<path to Pokemon RAW DATA>"

Writes:
    app/src/main/assets/pokedex.json
    app/src/main/assets/sprites/###.png and ###f.png (copied as-is)
"""
import csv
import json
import shutil
import sys
from pathlib import Path

PROJECT = Path(__file__).resolve().parent.parent
ASSETS = PROJECT / "app" / "src" / "main" / "assets"

# language_id in pokemon_species_flavor_text.csv
LANGUAGES = {"9": "en", "7": "es"}

# stats.csv identifier -> JSON key
STAT_KEYS = {
    "hp": "hp",
    "attack": "attack",
    "defense": "defense",
    "special-attack": "specialAttack",
    "special-defense": "specialDefense",
    "speed": "speed",
}

# Display data the CSVs don't have: Spanish names and colours per type.
TYPE_INFO = {
    "normal": ("Normal", "Normal", "#A8A878"),
    "fighting": ("Fighting", "Lucha", "#C03028"),
    "flying": ("Flying", "Volador", "#A890F0"),
    "poison": ("Poison", "Veneno", "#A040A0"),
    "ground": ("Ground", "Tierra", "#E0C068"),
    "rock": ("Rock", "Roca", "#B8A038"),
    "bug": ("Bug", "Bicho", "#A8B820"),
    "ghost": ("Ghost", "Fantasma", "#705898"),
    "steel": ("Steel", "Acero", "#B8B8D0"),
    "fire": ("Fire", "Fuego", "#F08030"),
    "water": ("Water", "Agua", "#6890F0"),
    "grass": ("Grass", "Planta", "#78C850"),
    "electric": ("Electric", "Eléctrico", "#F8D030"),
    "psychic": ("Psychic", "Psíquico", "#F85888"),
    "ice": ("Ice", "Hielo", "#98D8D8"),
    "dragon": ("Dragon", "Dragón", "#7038F8"),
    "dark": ("Dark", "Siniestro", "#705848"),
    "fairy": ("Fairy", "Hada", "#EE99AC"),
}

# Identifiers that don't become the right name by capitalising them.
SPECIAL_NAMES = {
    "nidoran-f": "Nidoran♀",
    "nidoran-m": "Nidoran♂",
    "farfetchd": "Farfetch'd",
    "mr-mime": "Mr. Mime",
}


def read_csv(folder: Path, name: str):
    with open(folder / name, encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def clean_text(text: str) -> str:
    # Flavor text keeps the game's line breaks and form feeds; join into one paragraph.
    return " ".join(text.replace("\f", " ").split())


def main(raw: Path) -> None:
    pokemon_rows = read_csv(raw, "pokemon.csv")
    stat_names = {r["id"]: r["identifier"] for r in read_csv(raw, "stats.csv")}
    type_names = {r["id"]: r["identifier"] for r in read_csv(raw, "types.csv")}

    stats: dict[str, dict] = {}
    for r in read_csv(raw, "pokemon_stats.csv"):
        key = STAT_KEYS.get(stat_names[r["stat_id"]])
        if key:
            stats.setdefault(r["pokemon_id"], {})[key] = int(r["base_stat"])

    types: dict[str, list] = {}
    for r in sorted(read_csv(raw, "pokemon_types.csv"), key=lambda r: int(r["slot"])):
        types.setdefault(r["pokemon_id"], []).append(type_names[r["type_id"]])

    texts: dict[str, dict] = {}
    for r in read_csv(raw, "pokemon_species_flavor_text.csv"):
        lang = LANGUAGES.get(r["language_id"])
        if lang:
            texts.setdefault(r["species_id"], {})[lang] = clean_text(r["flavor_text"])

    sprites_out = ASSETS / "sprites"
    sprites_out.mkdir(parents=True, exist_ok=True)

    pokemon = []
    for r in sorted(pokemon_rows, key=lambda r: int(r["id"])):
        pid = r["id"]
        num = f"{int(pid):03d}"
        female = raw / f"{num}f.png"
        shutil.copyfile(raw / f"{num}.png", sprites_out / f"{num}.png")
        if female.exists():
            shutil.copyfile(female, sprites_out / female.name)

        identifier = r["identifier"]
        pokemon.append({
            "id": int(pid),
            "identifier": identifier,
            "name": SPECIAL_NAMES.get(identifier, identifier.capitalize()),
            "types": types[pid],
            "height": int(r["height"]) / 10,   # decimetres -> metres
            "weight": int(r["weight"]) / 10,   # hectograms -> kilograms
            "baseExperience": int(r["base_experience"]),
            "stats": stats[pid],
            "description": texts[r["species_id"]],
            "sprite": f"sprites/{num}.png",
            "spriteFemale": f"sprites/{female.name}" if female.exists() else None,
        })

    used_types = {t for p in pokemon for t in p["types"]}
    type_list = [
        {"id": t, "name": {"en": en, "es": es}, "color": color}
        for t, (en, es, color) in TYPE_INFO.items()
        if t in used_types
    ]

    out = ASSETS / "pokedex.json"
    with open(out, "w", encoding="utf-8") as f:
        json.dump({"types": type_list, "pokemon": pokemon}, f, ensure_ascii=False, indent=2)
    print(f"Wrote {len(pokemon)} Pokemon and {len(type_list)} types to {out}")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
