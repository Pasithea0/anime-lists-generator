package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models;

import lombok.Getter;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Frederic Eßer
 */
@SuppressWarnings("JavadocLinkAsPlainText")
public enum AnimeSource {

//    anidb_id 	            Integer     anidb.net
//    anilist_id   	        Integer     anilist.co
//    animecountdown_id    	Integer     animecountdown.com
//    animenewsnetwork_id   Integer     animenewsnetwork.com
//    anime-planet_id  	    String      anime-planet.com
//    anisearch_id         	Integer     anisearch.com
//    imdb_id	            Integer
//    kitsu_id	            Integer     kitsu.app
//    livechart_id	        Integer     livechart.me
//    mal_id        	    Integer     myanimelist.net
//    simkl_id	            Integer     simkl.com
//    themoviedb_id	        Integer
//    thetvdb_id	        Integer


    ANIDB("anidb.net", "anidb") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setAnidb(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse AniDB ID '{}' because it isn't an Integer", id);
            }
        }
    },
    ANILIST("anilist.co", "anilist") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setAnilist(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Anilist ID '{}' because it isn't an Integer", id);
            }
        }
    },
    ANIMECOUNTDOWN("animecountdown.com", "animecountdown") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setAnimeCountdown(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Animecountdown ID '{}' because it isn't an Integer", id);
            }
        }
    },
    ANIMENEWSNETWORK("animenewsnetwork.com", "animenewsnetwork") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setAnimeNewsNetwork(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse AnimeNewsNetwork ID '{}' because it isn't an Integer", id);
            }
        }

        /**
         * overwrite the extractId Method because AnimeNewsNetwork uses a different pattern for the URL.
         * Example: "https://animenewsnetwork.com/encyclopedia/anime.php?id=25117"
         *
         * @param sourceUrl the sourceUrl that should be parsed
         * @return the ID as String
         */
        @Override
        public String extractId(String sourceUrl) {
            Pattern pattern = Pattern.compile("id=(\\d+)");
            Matcher matcher = pattern.matcher(sourceUrl);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                LOGGER.warn("Could not parse AnimeNewsNetwork URL and get the ID");
                return null;
            }
        }
    },
    ANIMEPLANET("anime-planet.com", "anime-planet") {
        /**
         * anime-planet uses a String for the ID. no need to parse it to an Integer, return it instead.
         * @param item the {@link AnimeItem} that we want to set the anime-planet ID for
         * @param id the ID as {@link String}
         */
        @Override
        public void setId(AnimeItem item, String id) {
            item.setAnimePlanet(id);
        }
    },
    ANISEARCH("anisearch.com", "anisearch") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setAnisearch(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Anisearch ID '{}' because it isn't an Integer", id);
            }
        }
    },
    KITSU("kitsu.app", "kitsu") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setKitsu(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Kitsu ID '{}' because it isn't an Integer", id);
            }
        }
    },
    LIVECHART("livechart.me", "livechart") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setLivechart(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Livechart ID '{}' because it isn't an Integer", id);
            }
        }
    },
    MYANIMELIST("myanimelist.net", "mal") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setMyanimelist(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse MyAnimeList ID '{}' because it isn't an Integer", id);
            }
        }
    },
    SIMKL("simkl.com", "simkl") {
        @Override
        public void setId(AnimeItem item, String id) {
            try {
                item.setSimkl(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                LOGGER.error("could not parse Simkl ID '{}' because it isn't an Integer", id);
            }
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeSource.class);

    private final String host;
    @Getter
    private final String key;

    AnimeSource(String host, String key) {
        this.host = host;
        this.key = key;
    }

    public abstract void setId(AnimeItem item, String id);

    /**
     * Get the AnimeSource from the provided source URL
     *
     * @param sourceUrl the source URL of the Anime Website
     * @return the Anime Source
     */
    public static Optional<AnimeSource> fromUrl(String sourceUrl) {
        try {
            URI uri = new URI(sourceUrl);
            String host = uri.getHost();
            return Arrays.stream(values())
                    .filter(source -> host != null && host.contains(source.host))
                    .findFirst()
                    .or(() -> {
                        LOGGER.warn("Unknown Anime source found: host: {}, URL: {}",host,sourceUrl);
                        return Optional.empty();
                    });
        } catch (URISyntaxException e) {
            LOGGER.error("could not parse URL");
            return Optional.empty();
        }
    }

    /**
     * Extract the ID from the source URL.
     * In most cases, this is simply the last part of the URL behind the last '/'
     *
     * @param sourceUrl the Source Url as {@link String}
     * @return the extracted ID as {@link String}
     */
    public String extractId(String sourceUrl) {
        try {
            URI uri = new URI(sourceUrl);
            String path = uri.getPath();
            String[] segments = path.split("/");
            return segments[segments.length - 1];
        } catch (URISyntaxException e) {
            LOGGER.error("could not parse URL while extracting the ID");
            return null;
        }
    }
}
