package net.fribbtastic.coding.animelistsgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeListsItem;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AnimeSource;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
@Data
public class AnimeItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeItem.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("type")
    private String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anidb_id")
    private Integer anidb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anilist_id")
    private Integer anilist;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("animecountdown_id")
    private Integer animeCountdown;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("animenewsnetwork_id")
    private Integer animeNewsNetwork;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anime-planet_id")
    private String animePlanet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anisearch_id")
    private Integer anisearch;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("imdb_id")
    private String imdb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("kitsu_id")
    private Integer kitsu;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("livechart_id")
    private Integer livechart;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("mal_id")
    private Integer myanimelist;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("simkl_id")
    private Integer simkl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("themoviedb_id")
    private TheMovieDBItem theMovieDb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tvdb_id")
    private Integer tvdb;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = SeasonFilter.class)
    @JsonProperty("season")
    private Season season;

    @JsonIgnore
    private transient Map<String, List<String>> relations;

    @JsonIgnore
    private transient String title;

    /**
     * Set the IDs by parsing the individual Source URLs
     *
     * @param sources the {@link ArrayList} containing all the Source URLs
     * @return the {@link AnimeItem} with the Set members
     */
    public static AnimeItem fromAODBSourceUrls(ArrayList<String> sources) {
        AnimeItem animeItem = new AnimeItem();

        for(String sourceUrl : sources) {
            AnimeSource.fromUrl(sourceUrl).ifPresent(source -> {
                String id = source.extractId(sourceUrl);
                if (id != null) {
                    source.setId(animeItem, id);
                }
            });
        }

        return animeItem;
    }

    /**
     * parse the Item from the anime-lists source to the standardized AnimeItem structure
     * @param item the anime-lists source item
     * @return the standardized AnimeItem
     */
    public static AnimeItem fromAnimeListsSource(AnimeListsItem item) {
        AnimeItem animeItem = new AnimeItem();

        // set the AniDB ID
        animeItem.setAnidb(item.getAnidbid());

        // set the IMDB ID
        animeItem.setImdb(item.getImdbid());

        // set the TMDB ID
        /*
        The anime-lists element uses two ways to set the TMDB ID
        1. with the tmdbid attribute
        2. with the tmdbtv attribute
         */
        TheMovieDBItem tmdbItem = null;
        if (item.getTmdbid() != null || item.getTmdbtv() != null) {
            tmdbItem = new TheMovieDBItem();

            if (item.getTmdbid() != null) {
                tmdbItem.setMovie(parseStringToInteger(item.getAnidbid(),"tmdb id", item.getTmdbid()));
            }
            if (item.getTmdbtv() != null) {
                tmdbItem.setTv(item.getTmdbtv());
            }
        }

        animeItem.setTheMovieDb(tmdbItem);

        // set TVDB ID
        animeItem.setTvdb(parseStringToInteger(item.getAnidbid(),"tvdb id", item.getTvdbid()));

        // add season information
        Season season = new Season();

        // set TMDB season
        if (item.getTmdbseason() != null                    // the tmdbseason needs to be available
                && !item.getTmdbseason().equals("a")        // and can't be set to "a"
                /*&& !item.getTmdbseason().equals("0")*/) {     // and can't be set to "0"

            season.setTheMovieDb(parseStringToInteger(item.getAnidbid(), "tmdb season", item.getTmdbseason()));
        }

        // set TVDB season
        if (item.getDefaulttvdbseason() != null                     // the defaulttvdbseason needs to be available
                && !item.getTvdbid().equals("movie")                // the tvdb id can't be set to "movie"
                && !item.getDefaulttvdbseason().equals("a")         // and can't be set to "a"
                /*&& !item.getDefaulttvdbseason().equals("0")*/) {      // and can't be set to "0"

            season.setThetvdb(parseStringToInteger(item.getAnidbid(), "tvdb season", item.getDefaulttvdbseason()));
        }

        // add season to anime item
        animeItem.setSeason(season);

        return animeItem;
    }

    /**
     * parse the ID as a string to an Integer
     *
     * @param itemId the ID of the item, for logging purposes
     * @param stringToParse the ID as a String that should be parsed
     * @return the parsed ID or null if it couldn't be parsed
     */
    public static Integer parseStringToInteger(Integer itemId, String type,String stringToParse) {
        if (NumberUtils.isCreatable(stringToParse)) {
            return NumberUtils.createInteger(stringToParse);
        } else {
            LOGGER.warn("[AniDB ID={}] could not parse {} '{}' because it isn't an Integer", itemId, type, stringToParse);
            return null;
        }
    }

    /**
     * merge the 'other' AnimeItem into this one.
     *
     * @param other the other AnimeItem that will be merged into this one
     */
    public void merge(AnimeItem other) {
        if (this.type == null) this.type = other.getType();
        if (this.anilist == null) this.anilist = other.getAnilist();
        if (this.animeCountdown == null) this.animeCountdown = other.getAnimeCountdown();
        if (this.animeNewsNetwork == null) this.animeNewsNetwork = other.getAnimeNewsNetwork();
        if (this.animePlanet == null) this.animePlanet = other.getAnimePlanet();
        if (this.anisearch == null) this.anisearch = other.getAnisearch();
        // The IMDB ID from the anime-lists source can be a comma-separated list
        // If this is the case, we ignore it and instead rely on the TMDB API to provide it through the external IDs instead
        if (this.imdb == null) {
            if (other.getImdb() != null && !other.getImdb().contains(",")) {
                this.imdb = other.getImdb();
            }
        }
        if (this.kitsu == null) this.kitsu = other.getKitsu();
        if (this.livechart == null) this.livechart = other.getLivechart();
        if (this.myanimelist == null) this.myanimelist = other.getMyanimelist();
        if (this.simkl == null) this.simkl = other.getSimkl();
        if (this.theMovieDb == null) this.theMovieDb = other.getTheMovieDb();
        if (this.tvdb == null) this.tvdb = other.getTvdb();

        if (this.season == null) {
            this.season = other.getSeason();
        }
    }

    @JsonIgnore
    public Map<String, List<String>> getIndexIdMap() {
        Map<String, List<String>> result = new HashMap<>();

        this.putIndexIdIfNotNull(result, "anidb", this.anidb);
        this.putIndexIdIfNotNull(result, "anilist", this.anilist);
        this.putIndexIdIfNotNull(result, "animecountdown", this.animeCountdown);
        this.putIndexIdIfNotNull(result, "animenewsnetwork", this.animeNewsNetwork);
        this.putIndexIdIfNotNull(result, "anime-planet", this.animePlanet);
        this.putIndexIdIfNotNull(result, "anisearch", this.anisearch);
        this.putIndexIdIfNotNull(result, "imdb", this.imdb);
        this.putIndexIdIfNotNull(result, "kitsu", this.kitsu);
        this.putIndexIdIfNotNull(result, "livechart", this.livechart);
        this.putIndexIdIfNotNull(result, "mal", this.myanimelist);
        this.putIndexIdIfNotNull(result, "simkl", this.simkl);
        this.putTheMovieDbIndexIdsIfNotNull(result);
        this.putIndexIdIfNotNull(result, "tvdb", this.tvdb);

        return result;
    }

    private void putIndexIdIfNotNull(Map<String, List<String>> map, String key, Object value) {
        if (value != null) {
            map.computeIfAbsent(key, ignored -> new ArrayList<>()).add(String.valueOf(value));
        }
    }

    private void putTheMovieDbIndexIdsIfNotNull(Map<String, List<String>> map) {
        if (this.theMovieDb == null) {
            return;
        }

        if (this.theMovieDb.getMovie() != null) {
            map.computeIfAbsent("themoviedb", ignored -> new ArrayList<>())
                    .add("movie:" + this.theMovieDb.getMovie());
        }

        if (this.theMovieDb.getTv() != null) {
            map.computeIfAbsent("themoviedb", ignored -> new ArrayList<>())
                    .add("tv:" + this.theMovieDb.getTv());
        }
    }

    public String getIdForSource(String source) {
        return switch (source) {
            case "anidb" -> this.anidb != null ? this.anidb.toString() : null;
            case "anilist" -> this.anilist != null ? this.anilist.toString() : null;
            case "animecountdown" -> this.animeCountdown != null ? this.animeCountdown.toString() : null;
            case "animenewsnetwork" -> this.animeNewsNetwork != null ? this.animeNewsNetwork.toString() : null;
            case "anime-planet" -> this.animePlanet != null ? this.animePlanet : null;
            case "anisearch" -> this.anisearch != null ? this.anisearch.toString() : null;
            case "imdb" -> this.imdb != null ? this.imdb : null;
            case "kitsu" -> this.kitsu != null ? this.kitsu.toString() : null;
            case "livechart" -> this.livechart != null ? this.livechart.toString() : null;
            case "mal" -> this.myanimelist != null ? this.myanimelist.toString() : null;
            case "simkl" -> this.simkl != null ? this.simkl.toString() : null;
            case "themoviedb" -> this.getTheMovieDbIndexId();
            case "tvdb" -> this.tvdb != null ? this.tvdb.toString() : null;
            default -> throw new IllegalStateException("Unexpected value: " + source);
        };
    }

    @JsonIgnore
    private String getTheMovieDbIndexId() {
        if (this.theMovieDb == null) {
            return null;
        }

        if (this.theMovieDb.getMovie() != null) {
            return "movie:" + this.theMovieDb.getMovie();
        }

        if (this.theMovieDb.getTv() != null) {
            return "tv:" + this.theMovieDb.getTv();
        }

        return null;
    }
}
