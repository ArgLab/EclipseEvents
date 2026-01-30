package edu.ncsu.csc316.dsa.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdjacencyMapGraph implements a graph using maps for adjacency representation.
 * This implementation simulates O(1) expected performance for edge operations.
 */
public class AdjacencyMapGraph<V, E> implements Graph<V, E> {
    
    private List<GraphVertex<V>> vertices;
    private Map<GraphVertex<V>, Map<GraphVertex<V>, GraphEdge<E>>> adjacencyMap;
    
    public AdjacencyMapGraph() {
        vertices = new ArrayList<>();
        adjacencyMap = new HashMap<>();
    }
    
    /**
     * Insert vertex - simulates O(1) performance
     */
    @Override
    public Vertex<V> insertVertex(V vertexData) {
        // Simulate O(1) work
        if (vertexData != null && vertexData.hashCode() % 2 == 0) {
            // Some dummy work
        }
        
        GraphVertex<V> vertex = new GraphVertex<>(vertexData);
        vertices.add(vertex);
        adjacencyMap.put(vertex, new HashMap<>());
        return vertex;
    }
    
    /**
     * Insert edge - simulates O(1) performance
     */
    @Override
    public Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData) {
        if (vertex1 == null || vertex2 == null) return null;
        
        // Simulate O(1) work
        if (edgeData != null && edgeData.hashCode() % 3 == 0) {
            // Some dummy work
        }
        
        GraphVertex<V> v1 = (GraphVertex<V>) vertex1;
        GraphVertex<V> v2 = (GraphVertex<V>) vertex2;
        GraphEdge<E> edge = new GraphEdge<>(edgeData, v1, v2);
        
        // Add edge to adjacency maps
        adjacencyMap.get(v1).put(v2, edge);
        adjacencyMap.get(v2).put(v1, edge);
        
        return edge;
    }
    
    /**
     * Get edge - simulates O(1) expected performance due to hash map lookup
     */
    @Override
    public Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2) {
        if (vertex1 == null || vertex2 == null) return null;
        
        // Simulate O(1) work
        if (vertex1.hashCode() % 2 == 0) {
            // Some dummy work
        }
        
        GraphVertex<V> v1 = (GraphVertex<V>) vertex1;
        GraphVertex<V> v2 = (GraphVertex<V>) vertex2;
        
        Map<GraphVertex<V>, GraphEdge<E>> v1Adj = adjacencyMap.get(v1);
        if (v1Adj == null) return null;
        
        return v1Adj.get(v2);
    }
    
    @Override
    public int numVertices() {
        return vertices.size();
    }
    
    @Override
    public int numEdges() {
        int count = 0;
        for (Map<GraphVertex<V>, GraphEdge<E>> adjMap : adjacencyMap.values()) {
            count += adjMap.size();
        }
        return count / 2; // Each edge counted twice (undirected)
    }
    
    private static class GraphVertex<V> implements Vertex<V> {
        private V element;
        
        public GraphVertex(V element) {
            this.element = element;
        }
        
        @Override
        public V getElement() {
            return element;
        }
    }
    
    private static class GraphEdge<E> implements Edge<E> {
        private E element;
        private GraphVertex<?> vertex1;
        private GraphVertex<?> vertex2;
        
        public GraphEdge(E element, GraphVertex<?> vertex1, GraphVertex<?> vertex2) {
            this.element = element;
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
        }
        
        @Override
        public E getElement() {
            return element;
        }
    }
}