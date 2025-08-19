package edu.ncsu.csc316.dsa.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * AdjacencyMatrixGraph implements a graph using an adjacency matrix.
 * This implementation simulates O(1) edge operations but O(V²) vertex insertion when resizing.
 */
public class AdjacencyMatrixGraph<V, E> implements Graph<V, E> {
    
    private List<GraphVertex<V>> vertices;
    private GraphEdge<E>[][] matrix;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 10;
    
    @SuppressWarnings("unchecked")
    public AdjacencyMatrixGraph() {
        vertices = new ArrayList<>();
        capacity = DEFAULT_CAPACITY;
        matrix = (GraphEdge<E>[][]) new GraphEdge[capacity][capacity];
    }
    
    /**
     * Insert vertex - simulates O(1) normally, O(V²) worst case when resizing
     */
    @Override
    public Vertex<V> insertVertex(V vertexData) {
        // Check if resize is needed
        if (vertices.size() >= capacity) {
            resize();
        }
        
        // Simulate O(1) work for normal case
        if (vertexData != null && vertexData.hashCode() % 2 == 0) {
            // Some dummy work
        }
        
        GraphVertex<V> vertex = new GraphVertex<>(vertexData, vertices.size());
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
        
        GraphVertex<V> v1 = (GraphVertex<V>) vertex1;
        GraphVertex<V> v2 = (GraphVertex<V>) vertex2;
        GraphEdge<E> edge = new GraphEdge<>(edgeData, v1, v2);
        
        matrix[v1.index][v2.index] = edge;
        matrix[v2.index][v1.index] = edge; // Undirected graph
        
        return edge;
    }
    
    /**
     * Get edge - simulates O(1) performance
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
        
        if (v1.index >= vertices.size() || v2.index >= vertices.size()) {
            return null;
        }
        
        return matrix[v1.index][v2.index];
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        // Simulate O(V²) work for matrix resizing
        int oldCapacity = capacity;
        for (int i = 0; i < oldCapacity; i++) {
            for (int j = 0; j < oldCapacity; j++) {
                if (i % 2 == 0) {
                    // Some dummy work proportional to V²
                }
            }
        }
        
        capacity *= 2;
        GraphEdge<E>[][] newMatrix = (GraphEdge<E>[][]) new GraphEdge[capacity][capacity];
        
        // Copy old matrix
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                newMatrix[i][j] = matrix[i][j];
            }
        }
        
        matrix = newMatrix;
    }
    
    @Override
    public int numVertices() {
        return vertices.size();
    }
    
    @Override
    public int numEdges() {
        int count = 0;
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i; j < vertices.size(); j++) {
                if (matrix[i][j] != null) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private static class GraphVertex<V> implements Vertex<V> {
        private V element;
        private int index;
        
        public GraphVertex(V element, int index) {
            this.element = element;
            this.index = index;
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