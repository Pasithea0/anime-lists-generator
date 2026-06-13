package net.fribbtastic.coding.animelistsgenerator.collections;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeCollection;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Frederic Eßer
 */
@Service
public class CollectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionService.class);

    private static final int MAX_DEGREE = 50;
    private static final int MAX_COLLECTION_SIZE = 100;

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

    /**
     * generate the collections separated by source
     * The graph will only include mutual relations between the anime items
     *
     * @param animeItems the list of AnimeItems
     * @return the map of collections separated by source
     */
    public Map<String, List<AnimeCollection>> generateCollections(Collection<AnimeItem> animeItems) {

        Map<String, Graph<String, DefaultEdge>> graphs = new HashMap<>();
        Map<String, Map<String, AnimeItem>> lookup = new HashMap<>();

        // initialize the grap and lookup
        for (AnimeItem anime : animeItems) {
            Map<String, List<String>> relations = anime.getRelations();
            // don't need to do anything if there are no relations
            if(relations == null) {
                continue;
            }

            for (String source : relations.keySet()) {
                // skip anime-planet because the IDs are the titles that makes it unusable for the collections
                if(source.equals("anime-planet")) {
                    continue;
                }

                graphs.computeIfAbsent(source, s -> new SimpleGraph<>(DefaultEdge.class));
                for (String id : anime.getIdForSource(source)) {
                    lookup.computeIfAbsent(source, s -> new HashMap<>()).put(id, anime);
                }
            }
        }

        // build the graph
        for (AnimeItem anime : animeItems) {
            Map<String, List<String>> relations = anime.getRelations();
            // again, don't need to do anything if there are no relations
            if(relations == null) {
                continue;
            }

            for (Map.Entry<String, List<String>> entry : relations.entrySet()) {
                String source = entry.getKey();
                // again, skip anime-planet
                if(source.equals("anime-planet")) {
                    continue;
                }

                Graph<String, DefaultEdge> graph = graphs.get(source);
                List<String> selfIds = anime.getIdForSource(source);
                if (graph == null || selfIds == null) {
                    continue;
                }

                selfIds.forEach(graph::addVertex);

                for (String relationId : entry.getValue()) {
                    graph.addVertex(relationId);

                    for (String selfId : selfIds) {
                        if (isMutualRelation(lookup.get(source), selfId, relationId)) {
                            if (!selfId.equals(relationId))
                            {
                                graph.addEdge(selfId, relationId);
                            }
                        }
                    }
                }
            }
        }

        // Remove large nodes to prevent giant clusters
        for (Graph<String, DefaultEdge> graph : graphs.values()) {

            List<String> toRemove = graph.vertexSet().stream()
                    .filter(v -> graph.degreeOf(v) > MAX_DEGREE)
                    .toList();

            toRemove.forEach(graph::removeVertex);
        }

        // extract collections
        Map<String, List<AnimeCollection>> result = new HashMap<>();

        for (Map.Entry<String, Graph<String, DefaultEdge>> entry : graphs.entrySet()) {
            String source = entry.getKey();
            Graph<String, DefaultEdge> graph = entry.getValue();
            ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
            List<AnimeCollection> collections = new ArrayList<>();

            for (Set<String> connectedSet : inspector.connectedSets()) {
                // skip very large collections
                if(connectedSet.size() > MAX_COLLECTION_SIZE) {
                    continue;
                }

                List<String> sorted = new ArrayList<>(connectedSet);
                sorted.sort(MIXED_ID_COMPARATOR);

                // determine the name of the collection
                String name = this.determineCollectionName(sorted, lookup.get(source));

                // convert the ID to the correct format
                List<Object> convertedIds = sorted.stream().map(this::convertId).toList();

                collections.add(new AnimeCollection(name, convertedIds));
            }

            result.put(source, collections);
        }

        return result;
    }

    private Object convertId(String id) {
        if (id != null && id.matches("\\d+")) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                LOGGER.warn("could not parse ID '{}' as Integer", id);
            }
        }
        return id;
    }

    /**
     * Determine the name of the collection based on the sorted list of IDs.
     *
     * @param sortedList the sorted list of IDs
     * @param lookup the lookup map for the source
     * @return the name of the collection
     */
    private String determineCollectionName(List<String> sortedList, Map<String, AnimeItem> lookup) {

        if(sortedList == null || sortedList.isEmpty()) {
            return "Unknown";
        }

        // if there is no lookup map, just return the first ID as fallback
        if(lookup == null) {
            return sortedList.getFirst();
        }

        for (String id : sortedList) {
            AnimeItem anime = lookup.get(id);
            if(anime != null && anime.getTitle() != null) {
                return anime.getTitle(); // return the earliest title that is available
            }
        }

        return sortedList.getFirst(); // fallback to the first ID if no title is available
    }

    /**
     * Only connect when A and B are mutually related
     *
     * @param sourceLookup the lookup map for the source
     * @param a the ID of the first anime
     * @param b the ID of the second anime
     * @return true if A and B are mutually related, false otherwise
     */
    private boolean isMutualRelation(Map<String, AnimeItem> sourceLookup, String a, String b) {
        if (sourceLookup == null) {
            return true;
        }

        AnimeItem other = sourceLookup.get(b);
        if (other == null || other.getRelations() == null) {
            return false;
        }

        for (List<String> relations : other.getRelations().values()) {
            if (relations.contains(a)) {
                return true;
            }
        }

        return false;
    }
}
