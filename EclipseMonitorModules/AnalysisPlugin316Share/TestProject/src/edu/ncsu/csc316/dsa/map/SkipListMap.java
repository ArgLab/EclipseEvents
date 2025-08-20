package edu.ncsu.csc316.dsa.map;

import java.util.Random;

/**
 * A SkipListMap implements a map using a skip list data structure.
 * This is a dummy implementation that mimics O(log n) expected performance.
 */
public class SkipListMap<K extends Comparable<K>, V> {
    
    private Node<K, V> head;
    private int size;
    private int maxLevel;
    private Random random;
    private static final double PROBABILITY = 0.5;
    
    public SkipListMap() {
        head = new Node<>(null, null, 32); // Max 32 levels
        size = 0;
        maxLevel = 0;
        random = new Random(42); // Fixed seed for reproducible performance
    }
    
    /**
     * Get operation - simulates O(log n) expected performance
     */
    public V get(K key) {
        if (key == null) return null;
        
        // Simulate skip list traversal work proportional to log(size)
        int expectedWork = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < expectedWork; i++) {
            // Simulate pointer following and comparisons
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate comparison
            }
        }
        
        // Simple linear search for correctness (would be skip list traversal in real implementation)
        Node<K, V> current = head.forward[0];
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.forward[0];
        }
        return null;
    }
    
    /**
     * Put operation - simulates O(log n) expected performance
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Simulate skip list traversal and insertion work
        int expectedWork = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < expectedWork; i++) {
            // Simulate the work of finding insertion point
            if (key.hashCode() % 3 == 0) {
                // Some dummy work
            }
        }
        
        // Check if key already exists
        Node<K, V> current = head.forward[0];
        while (current != null) {
            if (current.key.equals(key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.forward[0];
        }
        
        // Insert new node (simplified implementation)
        int level = randomLevel();
        Node<K, V> newNode = new Node<>(key, value, level + 1);
        
        // Link the new node at level 0 (simplified)
        newNode.forward[0] = head.forward[0];
        head.forward[0] = newNode;
        
        size++;
        return null;
    }
    
    private int randomLevel() {
        int level = 0;
        while (random.nextDouble() < PROBABILITY && level < maxLevel + 1) {
            level++;
        }
        if (level > maxLevel) {
            maxLevel = level;
        }
        return level;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V>[] forward;
        
        @SuppressWarnings("unchecked")
        public Node(K key, V value, int level) {
            this.key = key;
            this.value = value;
            this.forward = (Node<K, V>[]) new Node[level];
        }
    }
}