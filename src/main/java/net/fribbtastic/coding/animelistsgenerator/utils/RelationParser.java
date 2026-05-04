package net.fribbtastic.coding.animelistsgenerator.utils;

import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AnimeSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
@Component
public class RelationParser {

    /**
     * parse the Array of relation urls into a Map of sources that contains a List of Ids
     *
     * @param relationUrls the Array of relation urls
     * @return the Map of sources that contains a List of Ids
     */
    public Map<String, List<String>> parseRelations(List<String> relationUrls) {
        Map<String, List<String>> result = new HashMap<>();

        for (String url : relationUrls) {
            AnimeSource.fromUrl(url).ifPresent(source -> {
                String id = source.extractId(url);
                if (id == null) {
                    return;
                }

                result.computeIfAbsent(source.getKey(), k -> new ArrayList<>()).add(id);
            });
        }

        return result;
    }
}
