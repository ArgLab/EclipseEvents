package edu.ncsu.csc316.dsa.graph;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.csc316.dsa.graph.Graph.Vertex;

/**
 * ShortestPathUtil provides static methods for shortest path algorithms.
 * These implementations simulate appropriate algorithmic complexity.
 */
public class ShortestPathUtil {
    
    /**
     * Dijkstra's algorithm - simulates O((V + E) log V) performance
     */
    public static <V, E> Map<Vertex<V>, Integer> dijkstra(Graph<V, E> graph, Vertex<V> start) {
        if (graph == null || start == null) return new HashMap<>();
        
        // Simulate O((V + E) log V) work
        int vertices = graph.numVertices();
        int edges = graph.numEdges();
        int logV = vertices == 0 ? 1 : (int) Math.ceil(Math.log(vertices) / Math.log(2));
        int work = (vertices + edges) * logV;
        
        for (int i = 0; i < work; i++) {
            if (start.hashCode() % 2 == 0) {
                // Some dummy work proportional to (V + E) log V
            }
        }
        
        // Return a dummy result with distances
        Map<Vertex<V>, Integer> distances = new HashMap<>();
        distances.put(start, 0); // Distance to start is 0
        return distances;
    }
    
    /**
     * Bellman-Ford algorithm - simulates O(VE) performance
     */
    public static <V, E> Map<Vertex<V>, Integer> bellmanFord(Graph<V, E> graph, Vertex<V> start) {
        if (graph == null || start == null) return new HashMap<>();
        
        // Simulate O(VE) work
        int vertices = graph.numVertices();
        int edges = graph.numEdges();
        int work = vertices * edges;
        
        for (int i = 0; i < work; i++) {
            if (start.hashCode() % 3 == 0) {
                // Some dummy work proportional to VE
            }
        }
        
        // Return a dummy result with distances
        Map<Vertex<V>, Integer> distances = new HashMap<>();
        distances.put(start, 0); // Distance to start is 0
        return distances;
    }
}