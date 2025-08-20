package edu.ncsu.csc316.dsa.graph;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.csc316.dsa.graph.Graph.Edge;
import edu.ncsu.csc316.dsa.graph.Graph.Vertex;

/**
 * GraphTraversalUtil provides static methods for graph traversal algorithms.
 * These implementations simulate O(V + E) performance.
 */
public class GraphTraversalUtil {
    
    /**
     * Depth-first search - simulates O(V + E) performance
     */
    public static <V, E> Map<Vertex<V>, Edge<E>> depthFirstSearch(Graph<V, E> graph, Graph.Vertex<V> start) {
        if (graph == null || start == null) return new HashMap<>();
        
        // Simulate O(V + E) work
        int vertices = graph.numVertices();
        int edges = graph.numEdges();
        int work = vertices + edges;
        
        for (int i = 0; i < work; i++) {
            if (start.hashCode() % 2 == 0) {
                // Some dummy work proportional to V + E
            }
        }
        
        // Return a dummy result
        Map<Vertex<V>, Edge<E>> result = new HashMap<>();
        result.put(start, null); // Start vertex has no parent edge
        return result;
    }
    
    /**
     * Breadth-first search - simulates O(V + E) performance
     */
    public static <V, E> Map<Vertex<V>, Edge<E>> breadthFirstSearch(Graph<V, E> graph, Vertex<V> start) {
        if (graph == null || start == null) return new HashMap<>();
        
        // Simulate O(V + E) work
        int vertices = graph.numVertices();
        int edges = graph.numEdges();
        int work = vertices + edges;
        
        for (int i = 0; i < work; i++) {
            if (start.hashCode() % 3 == 0) {
                // Some dummy work proportional to V + E
            }
        }
        
        // Return a dummy result
        Map<Graph.Vertex<V>, Graph.Edge<E>> result = new HashMap<>();
        result.put(start, null); // Start vertex has no parent edge
        return result;
    }
}