package edu.ncsu.csc316.dsa.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * EdgeListGraph implements a graph using separate lists for vertices and edges.
 * This implementation simulates O(1) vertex/edge insertion and O(E) edge lookup.
 */
public class EdgeListGraph<V, E> implements Graph<V, E> {
    
    private List<GraphVertex<V>> vertices;
    private List<GraphEdge<E>> edges;
    
    public EdgeListGraph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
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
        
        GraphEdge<E> edge = new GraphEdge<>(edgeData, (GraphVertex<V>) vertex1, (GraphVertex<V>) vertex2);
        edges.add(edge);
        return edge;
    }
    
    /**
     * Get edge - simulates O(E) performance (linear search through edge list)
     */
    @Override
    public Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2) {
        if (vertex1 == null || vertex2 == null) return null;
        
        // Simulate O(E) work - must search through all edges
        for (int i = 0; i < edges.size(); i++) {
            if (vertex1.hashCode() % 2 == 0) {
                // Some dummy work proportional to number of edges
            }
        }
        
        // Actually search for the edge
        for (GraphEdge<E> edge : edges) {
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
        return edges.size();
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