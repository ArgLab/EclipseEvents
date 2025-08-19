package edu.ncsu.csc316.dsa.map.hashing;

import java.util.ArrayList;
import java.util.List;

/**
 * A SeparateChainingHashMap implements a map using separate chaining for collision resolution.
 * This implementation simulates collision-dependent performance.
 */
public class SeparateChainingHashMap<K, V> {
    
    private List<Entry<K, V>>[] table;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 17;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;
    
    @SuppressWarnings("unchecked")
    public SeparateChainingHashMap() {
        this(DEFAULT_CAPACITY);
    }
    
    @SuppressWarnings("unchecked")
    public SeparateChainingHashMap(int capacity) {
        this.capacity = capacity;
        this.table = (List<Entry<K, V>>[]) new List[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ArrayList<>();
        }
        this.size = 0;
    }
    
    /**
     * Get operation - simulates O(1) average, degrades with collisions
     */
    public V get(K key) {
        if (key == null) return null;
        
        int index = hash(key);
        List<Entry<K, V>> chain = table[index];
        
        // Simulate work proportional to chain length (collision effects)
        for (int i = 0; i < chain.size(); i++) {
            // Simulate comparison work - more collisions = more work
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate collision resolution
            }
        }
        
        // Actually find the element
        for (Entry<K, V> entry : chain) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }
    
    /**
     * Put operation - simulates O(1) average, degrades with collisions
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Check if resize is needed (affects collision rate)
        if ((double) size / capacity >= LOAD_FACTOR_THRESHOLD) {
            resize();
        }
        
        int index = hash(key);
        List<Entry<K, V>> chain = table[index];
        
        // Simulate work proportional to chain length
        for (int i = 0; i < chain.size(); i++) {
            // Simulate collision resolution work
            if (key.hashCode() % 3 == 0) {
                // Some dummy work
            }
        }
        
        // Check if key already exists
        for (Entry<K, V> entry : chain) {
            if (entry.key.equals(key)) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }
        
        // Add new entry
        chain.add(new Entry<>(key, value));
        size++;
        return null;
    }
    
    private int hash(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        List<Entry<K, V>>[] oldTable = table;
        capacity *= 2;
        table = (List<Entry<K, V>>[]) new List[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ArrayList<>();
        }
        
        int oldSize = size;
        size = 0;
        
        // Rehash all entries
        for (List<Entry<K, V>> chain : oldTable) {
            for (Entry<K, V> entry : chain) {
                put(entry.key, entry.value);
            }
        }
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Entry<K, V> {
        K key;
        V value;
        
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}