package edu.ncsu.csc316.dsa.graph;

/**
 * Basic graph interface for simple implementations
 */
public interface Graph<V, E> {
    
    /**
     * Vertex interface
     */
    interface Vertex<V> {
        V getElement();
    }
    
    /**
     * Edge interface  
     */
    interface Edge<E> {
        E getElement();
    }
    
    Vertex<V> insertVertex(V vertexData);
    Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData);
    Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2);
    
    int numVertices();
    int numEdges();
}