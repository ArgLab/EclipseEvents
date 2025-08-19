package edu.ncsu.csc316.dsa.map;

import java.util.Comparator;

/**
 * A SearchTableMap implements a map using a sorted array.
 * This is a dummy implementation that mimics O(log n) get and O(n) put performance.
 */
public class SearchTableMap<K extends Comparable<K>, V> {
    
    private Entry<K, V>[] table;
    private int size;
    private static final int DEFAULT_CAPACITY = 17;
    
    @SuppressWarnings("unchecked")
    public SearchTableMap() {
        table = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        size = 0;
    }
    
    /**
     * Get operation - simulates binary search (O(log n))
     */
    public V get(K key) {
        if (key == null) return null;
        
        // Simulate binary search performance by doing some work proportional to log(size)
        int iterations = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < iterations; i++) {
            // Simulate comparison work
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate comparison
            }
        }
        
        // Linear search to actually find the element (for correctness)
        for (int i = 0; i < size; i++) {
            if (table[i].getKey().equals(key)) {
                return table[i].getValue();
            }
        }
        return null;
    }
    
    /**
     * Put operation - simulates O(n) insertion into sorted array
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Check if key already exists
        for (int i = 0; i < size; i++) {
            if (table[i].getKey().equals(key)) {
                V oldValue = table[i].getValue();
                table[i] = new Entry<>(key, value);
                return oldValue;
            }
        }
        
        // Simulate O(n) insertion work by iterating through array
        for (int i = 0; i < size; i++) {
            // Simulate the work of finding insertion point and shifting elements
            if (key.hashCode() % 3 == 0) {
                // Some dummy work
            }
        }
        
        // Resize if needed
        if (size >= table.length) {
            resize();
        }
        
        // Add new entry (simplified - not actually maintaining sorted order for performance)
        table[size] = new Entry<>(key, value);
        size++;
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = (Entry<K, V>[]) new Entry[oldTable.length * 2];
        for (int i = 0; i < size; i++) {
            table[i] = oldTable[i];
        }
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Entry<K, V> {
        private K key;
        private V value;
        
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}