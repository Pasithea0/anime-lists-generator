package net.fribbtastic.coding.animelistsgenerator.models;

/**
 * @author Frederic Eßer
 */
public class EpisodeOffsetFilter {

    @Override
    public boolean equals(Object obj) {
        // If the Object is null, return true to omit it from the JSON
        if (obj == null) {
            return true;
        }
        // don't do anything if the Object is not an EpisodeOffset
        if(!(obj instanceof EpisodeOffset episodeOffset)) {
            return false;
        }

        Integer tvdb = episodeOffset.getThetvdb();
        Integer tmdb = episodeOffset.getTheMovieDb();

        // return true when both tvdb and tmdb are null
        return tvdb == null && tmdb == null;
    }
}
