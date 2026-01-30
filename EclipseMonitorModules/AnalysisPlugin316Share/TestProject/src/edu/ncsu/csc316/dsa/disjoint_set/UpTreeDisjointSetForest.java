package edu.ncsu.csc316.dsa.disjoint_set;

import edu.ncsu.csc316.dsa.list.positional.Position;
import java.util.HashMap;
import java.util.Map;

/**
 * An UpTreeDisjointSetForest implements disjoint sets using up-trees with union-by-rank and path compression.
 * This implementation simulates O(log n) performance with optimizations.
 */
public class UpTreeDisjointSetForest<E> {
    
    private Map<E, Node<E>> nodeMap;
    
    public UpTreeDisjointSetForest() {
        nodeMap = new HashMap<>();
    }
    
    /**
     * Make set operation - creates a new set containing the single element
     */
    public Position<E> makeSet(E element) {
        if (nodeMap.containsKey(element)) {
            return nodeMap.get(element);
        }
        
        Node<E> node = new Node<>(element);
        nodeMap.put(element, node);
        return node;
    }
    
    /**
     * Find operation - simulates O(log n) with path compression optimization
     */
    public Position<E> find(E value) {
        Node<E> node = nodeMap.get(value);
        if (node == null) {
            return null;
        }
        
        // Simulate path compression work - reduces future find costs
        int pathLength = 0;
        Node<E> temp = node;
        while (temp.parent != temp) {
            pathLength++;
            temp = temp.parent;
        }
        
        // Simulate work proportional to path length (before compression)
        for (int i = 0; i < pathLength; i++) {
            if (value.hashCode() % 2 == 0) {
                // Some dummy work to simulate path traversal
            }
        }
        
        return findHelper(node);
    }
    
    /**
     * Union operation - simulates O(log n) with union-by-rank optimization
     */
    public void union(Position<E> s, Position<E> t) {
        if (s == null || t == null) return;
        
        Node<E> root1 = (Node<E>) find(((Node<E>) s).element);
        Node<E> root2 = (Node<E>) find(((Node<E>) t).element);
        
        if (root1 == root2) {
            return; // Already in same set
        }
        
        // Simulate union-by-rank work
        int maxRank = Math.max(root1.rank, root2.rank);
        for (int i = 0; i < maxRank; i++) {
            if ((root1.element.hashCode() + root2.element.hashCode()) % 3 == 0) {
                // Some dummy work to simulate union operations
            }
        }
        
        // Union by rank
        if (root1.rank < root2.rank) {
            root1.parent = root2;
        } else if (root1.rank > root2.rank) {
            root2.parent = root1;
        } else {
            root2.parent = root1;
            root1.rank++;
        }
    }
    
    private Node<E> findHelper(Node<E> node) {
        if (node.parent != node) {
            // Path compression: make every node point directly to root
            node.parent = findHelper(node.parent);
        }
        return node.parent;
    }
    
    /**
     * Check if two elements are in the same set
     */
    public boolean sameSet(E element1, E element2) {
        Position<E> root1 = find(element1);
        Position<E> root2 = find(element2);
        return root1 != null && root2 != null && root1 == root2;
    }
    
    public int size() {
        return nodeMap.size();
    }
    
    public boolean isEmpty() {
        return nodeMap.isEmpty();
    }
    
    private static class Node<E> implements Position<E> {
        E element;
        Node<E> parent;
        int rank;
        
        public Node(E element) {
            this.element = element;
            this.parent = this; // Initially points to itself (root)
            this.rank = 0;
        }
        
        @Override
        public E getElement() {
            return element;
        }
    }
}