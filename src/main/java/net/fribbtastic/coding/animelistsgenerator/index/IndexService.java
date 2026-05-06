package net.fribbtastic.coding.animelistsgenerator.index;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeCollection;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Frederic Eßer
 */
@Service
public class IndexService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IndexService.class);

    private static final Comparator<String> MIXED_ID_COMPARATOR = (a, b) -> {
        boolean aNumeric = a.matches("\\d+");
        boolean bNumeric = b.matches("\\d+");

        if (aNumeric && bNumeric) {
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        }

        // compare IMDB Ids
        if (a.startsWith("tt") && b.startsWith("tt")) {
            try {
                int aNum = Integer.parseInt(a.substring(2));
                int bNum = Integer.parseInt(b.substring(2));

                return Integer.compare(aNum, bNum);
            } catch (NumberFormatException e) {
                LOGGER.warn("could not parse IMDB ID '{}' and '{}'", a, b);
            }
        }

        return a.compareTo(b);
    };

    public Map<String, Map<String, Map<String, List<Integer>>>> combineIndexMaps(
            Map<String, Map<String, List<Integer>>> animeListIndexMap,
            Map<String, Map<String, List<Integer>>> collectionIndexMap
    ) {
        LOGGER.info("Generating index maps");

        Map<String, Map<String, Map<String, List<Integer>>>> combinedIndexMap = new TreeMap<>();

        // iterate over every anime list entry and add it to the combined index map
        for (Map.Entry<String, Map<String, List<Integer>>> animeListEntry : animeListIndexMap.entrySet()) {
            String source = animeListEntry.getKey();
            Map<String, List<Integer>> innerMap = animeListEntry.getValue();

            for (Map.Entry<String, List<Integer>> entry : innerMap.entrySet()) {
                String id = entry.getKey();
                List<Integer> indices = entry.getValue();

                combinedIndexMap.computeIfAbsent(source, s -> new TreeMap<>(MIXED_ID_COMPARATOR))
                        .computeIfAbsent(id, i -> new HashMap<>())
                        .put("anime-list", indices);
            }
        }

        // iterate over every collection entry and add it to the combined index map
        for (Map.Entry<String, Map<String, List<Integer>>> collectionEntry : collectionIndexMap.entrySet()) {
            String source = collectionEntry.getKey();
            Map<String, List<Integer>> innerMap = collectionEntry.getValue();

            for (Map.Entry<String, List<Integer>> entry : innerMap.entrySet()) {
                String id = entry.getKey();
                List<Integer> indices = entry.getValue();

                combinedIndexMap.computeIfAbsent(source, s -> new TreeMap<>(MIXED_ID_COMPARATOR))
                        .computeIfAbsent(id, i -> new HashMap<>())
                        .put("collection", indices);
            }
        }

        return combinedIndexMap;
    }

    /**
     * Generate the list of indexes of the merged list for each anime item
     *
     * @param mergedList the list containing the merged anime items
     * @return the map of indexes
     */
    public Map<String, Map<String, List<Integer>>> generateAnimeListIndex(ArrayList<AnimeItem> mergedList) {
        LOGGER.info("Generating index of merged anime lists");

        Map<String, Map<String, List<Integer>>> shardIndexMap = new TreeMap<>();

        // iterate over every item in the merged list
        for (int i = 0; i < mergedList.size(); i++) {
            AnimeItem item = mergedList.get(i);

            // iterate over every available ID in the item
            for (Map.Entry<String, String> entry : item.getIdMap().entrySet()) {
                String source = entry.getKey(); // e.g. "anidb"
                String value = entry.getValue(); // e.g. "1"

                shardIndexMap.computeIfAbsent(source, s -> new TreeMap<>(MIXED_ID_COMPARATOR))
                        .computeIfAbsent(value, v -> new ArrayList<>())
                        .add(i);
            }
        }

        return shardIndexMap;
    }

    /**
     * Generate the list of indices for the collections
     *
     * @param collections the list of collections
     * @return the map of indices
     */
    public Map<String, Map<String, List<Integer>>> generateCollectionIndex(Map<String, List<AnimeCollection>> collections) {
        LOGGER.info("Generating index of Anime collections");

        Map<String, Map<String, List<Integer>>> shardIndexMap = new TreeMap<>();

        if(collections == null) {
            return shardIndexMap;
        }

        for (Map.Entry<String, List<AnimeCollection>> sourceEntry : collections.entrySet()) {
            String source = sourceEntry.getKey();
            List<AnimeCollection> sourceCollections = sourceEntry.getValue();

            for (int collectionIndex = 0; collectionIndex < sourceCollections.size(); collectionIndex++) {
                AnimeCollection collection = sourceCollections.get(collectionIndex);

                for (Object rawId : collection.getIdList()) {
                    String id = String.valueOf(rawId);

                    shardIndexMap.computeIfAbsent(source, s -> new TreeMap<>(MIXED_ID_COMPARATOR))
                            .computeIfAbsent(id, v -> new ArrayList<>())
                            .add(collectionIndex);
                }

            }
        }

        return shardIndexMap;
    }
}
