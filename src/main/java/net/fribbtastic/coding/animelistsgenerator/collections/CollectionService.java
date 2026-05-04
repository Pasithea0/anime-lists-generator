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
                String id = anime.getIdForSource(source);
                if(id != null) {
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
                String selfId = anime.getIdForSource(source);
                if (graph == null || selfId == null) {
                    continue;
                }

                graph.addVertex(selfId);

                for (String relationId : entry.getValue()) {
                    graph.addVertex(relationId);

                    if (this.isMutualRelation(lookup.get(source), selfId, relationId)) {
                        if (!selfId.equals(relationId)) {
                            graph.addEdge(selfId, relationId);
                        }
                    }
                }
            }
        }

        // Remove Hub nodes to prevent giant clusters
//        for (Graph<String, DefaultEdge> graph : graphs.values()) {
//            graph.vertexSet().removeIf(v -> graph.inDegreeOf(v) > MAX_DEGREE);
//        }

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

                List<String> sortedList = new ArrayList<>(connectedSet);
                sortedList.sort(MIXED_ID_COMPARATOR);

                String name = this.determineCollectionName(sortedList, lookup.get(source));

                collections.add(new AnimeCollection(name, sortedList));
            }

            result.put(source, collections);
        }

        return result;
    }

    /**
     * Determine the name of the collection based on the sorted list of IDs.
     *
     * @param sortedList the sorted list of IDs
     * @param lookup the lookup map for the source
     * @return the name of the collection
     */
    private String determineCollectionName(List<String> sortedList, Map<String, AnimeItem> lookup) {
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

    /**
     * generate the collections separated by source
     *
     * @param animeItems the list of AnimeItems
     * @return the map of collections
     */
//    public Map<String, List<List<String>>> generateCollections(Collection<AnimeItem> animeItems) {
//        Map<String, Graph<String, DefaultEdge>> graphs = new HashMap<>();
//
//        // Initialize the Graph for each source
//        for (AnimeItem anime : animeItems) {
//            // skip items without relations
//            if (anime.getRelations() == null) {
//                continue;
//            }
//
//            for (String source : anime.getRelations().keySet()) {
//                graphs.computeIfAbsent(source, s -> new SimpleGraph<>(DefaultEdge.class));
//            }
//        }
//
//        // build the graph by adding vertexes and edges
//        for (AnimeItem anime : animeItems) {
//
//            Map<String, List<String>> relations = anime.getRelations();
//
//            // again, skip items without relations
//            if (relations == null) {
//                continue;
//            }
//
//            for (Map.Entry<String, List<String>> relationEntry : relations.entrySet()) {
//                String source = relationEntry.getKey();
//                List<String> relatedIdList = relationEntry.getValue();
//
//                Graph<String, DefaultEdge> graph = graphs.get(source);
//                String selfId = anime.getIdForSource(source);
//                // skip if there is no graph for this source
//                if (graph == null || selfId == null) {
//                    continue;
//                }
//
//                graph.addVertex(selfId);
//
//                for (String relation : relatedIdList) {
//
//                    graph.addVertex(relation);
//                    graph.addEdge(selfId, relation);
//                }
//            }
//        }
//
//        // extract collections for each source
//        Map<String, List<List<String>>> result = new HashMap<>();
//
//        for (Map.Entry<String, Graph<String, DefaultEdge>> graphEntry : graphs.entrySet()) {
//            String source = graphEntry.getKey();
//            Graph<String, DefaultEdge> graph = graphEntry.getValue();
//
//            ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
//
//            // sorting
//            List<Set<String>> connectedSets = inspector.connectedSets();
//            List<List<String>> sortedCollections = new ArrayList<>();
//
//            for (Set<String> set : connectedSets) {
//                List<String> sortedList = new ArrayList<>(set);
//                sortedList.sort(MIXED_ID_COMPARATOR);
//                sortedCollections.add(sortedList);
//            }
//
//            result.put(source, sortedCollections);
//        }
//
//        return result;
//    }
}
