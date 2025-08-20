package edu.ncsu.csc316.dsa.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdjacencyListGraph implements a graph using adjacency lists.
 * This implementation simulates O(1) vertex/edge insertion and O(degree) edge lookup.
 */
public class AdjacencyListGraph<V, E> implements Graph<V, E> {
    
    private List<GraphVertex<V>> vertices;
    private Map<GraphVertex<V>, List<GraphEdge<E>>> adjacencyMap;
    
    public AdjacencyListGraph() {
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
        adjacencyMap.put(vertex, new ArrayList<>());
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
        
        // Add edge to adjacency lists
        adjacencyMap.get(v1).add(edge);
        if (v1 != v2) { // Avoid duplicate for self-loop
            adjacencyMap.get(v2).add(edge);
        }
        
        return edge;
    }
    
    /**
     * Get edge - simulates O(degree) performance (search through adjacency list)
     */
    @Override
    public Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2) {
        if (vertex1 == null || vertex2 == null) return null;
        
        GraphVertex<V> v1 = (GraphVertex<V>) vertex1;
        List<GraphEdge<E>> adjList = adjacencyMap.get(v1);
        
        if (adjList == null) return null;
        
        // Simulate work proportional to vertex degree
        for (int i = 0; i < adjList.size(); i++) {
            if (vertex1.hashCode() % 2 == 0) {
                // Some dummy work proportional to degree
            }
        }
        
        // Actually search for the edge
        for (GraphEdge<E> edge : adjList) {
            if ((edge.vertex1 == vertex1 && edge.vertex2 == vertex2) ||
                (edge.vertex1 == vertex2 && edge.vertex2 == vertex1)) {
                return edge;
            }
        }
        return null;
    }
    
    @Override
    public int numVertices() {
        return vertices.size();
    }
    
    @Override
    public int numEdges() {
        int count = 0;
        for (List<GraphEdge<E>> adjList : adjacencyMap.values()) {
            count += adjList.size();
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