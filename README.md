# anime-lists-generator

Merges [Anime-Offline-Database](https://github.com/manami-project/anime-offline-database) (metadata) with [Anime-Lists](https://github.com/Anime-Lists/anime-lists) (episode mappings) by AniDB ID, optionally enriches with TheMovieDB IDs, and outputs combined JSON files.

## Setup & Build (Java)

**Prerequisites:** Java 21, Maven

```bash
cp src/main/resources/application.properties.sample src/main/resources/application.properties
# edit project.path (output directory) and project.apikey.themoviedb (optional)
./mvnw clean package -DskipTests
java -jar target/anime-lists-generator-*.jar --spring.profiles.active=prod
```

### Application Properties

| Property | Required | Description |
|---|---|---|
| `project.path` | Yes | Output directory for generated JSON files |
| `project.apikey.themoviedb` | No | TMDB API key for enriching entries with TMDB IDs |

### Output Files

All generated in the configured `project.path`:

| File | Description |
|---|---|
| `anime-list-full.json` | Merged dataset (pretty-printed) |
| `anime-list-mini.json` | Merged dataset (minified) |
| `anime-offline-database-reduced.json` | Condensed AODB data |
| `anime-lists-reduced.json` | Condensed anime-lists data |
| `collections/*_collection.json` | Collections grouped by source |
| `indices/*_index.json` | Lookup indices by source |

### Tests

Download test files from the [test release](https://github.com/Fribb/anime-lists-generator/releases/tag/test) and place them in `src/test/resources/files/`, then:

```bash
./mvnw test
```

## GitHub Workflow

A scheduled workflow (`.github/workflows/generate.yml`) runs every Sunday at 06:00 UTC. It builds the generator, runs it, and commits generated files to the `data/` directory. You can also trigger it manually from the Actions tab.

The workflow accepts an optional `TMDB_API_KEY` repository secret.

## Go Module (`go/`)

A Go package for looking up anime by MyAnimeList (MAL) ID, returning the corresponding TMDB ID, TVDB ID, season mapping, and episode offset.

### Usage as a Library

```go
import "github.com/Pasithea0/anime-lists-generator/go/anime"

lookup, err := anime.NewLookup("data/anime-list-full.json")
if err != nil {
    // handle error
}

result := lookup.LookupMAL(12345)
if result == nil {
    // not found
}

// result.TMDBID  -> { "movie": [123], "tv": 456 } or nil
// result.TVDBID  -> 789 or nil
// result.Season  -> { "tvdb": 1, "tmdb": 2 } or nil  — season number mapping
// result.EpisodeOffset -> { "tvdb": 24, "tmdb": 24 } or nil — episode offset mapping
```

### Usage as an HTTP Server

```bash
go run ./go/cmd/anime-lists-server/ -data data/anime-list-full.json
```

Flags:

| Flag | Default | Description |
|---|---|---|
| `-addr` | `:8080` | Server listen address |
| `-data` | `data/anime-list-full.json` | Path to `anime-list-full.json` |

#### Endpoint

```
GET /lookup?mal-id=1
```

**200 OK:**
```json
{
  "mal_id": 1,
  "tmdb_id": {
    "movie": [550],
    "tv": 60625
  },
  "tvdb_id": 123456,
  "season": {
    "tvdb": 1,
    "tmdb": 2
  },
  "episode_offset": {
    "tvdb": 24,
    "tmdb": 24
  }
}
```

**400 Bad Request:** `mal-id` parameter missing or not an integer.

**404 Not Found:** No entry for the given MAL ID.

### Episode Mapping

The `season` and `episode_offset` fields bridge the gap between how TMDB structures seasons/episodes vs. how they're listed in anime sources. For example, if a show has 48 episodes but TMDB splits them as season 1 (episodes 1–24) and season 2 (episodes 25–48), the entry would have `season.tmdb: 2` and `episode_offset.tmdb: 24` — meaning TMDB season 2 starts at anime episode 25, so you compute: `anime_episode = tmdb_episode + episode_offset`.
