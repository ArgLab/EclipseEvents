package edu.ncsu.csc316.dsa.map.hashing;

/**
 * A LinearProbingHashMap implements a map using linear probing for collision resolution.
 * This implementation simulates collision-dependent performance with clustering effects.
 */
public class LinearProbingHashMap<K, V> {
    
    private Entry<K, V>[] table;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 17;
    private static final double LOAD_FACTOR_THRESHOLD = 0.5; // Lower threshold for open addressing
    private static final Entry<?, ?> DELETED = new Entry<>(null, null);
    
    @SuppressWarnings("unchecked")
    public LinearProbingHashMap() {
        this(DEFAULT_CAPACITY);
    }
    
    @SuppressWarnings("unchecked")
    public LinearProbingHashMap(int capacity) {
        this.capacity = capacity;
        this.table = (Entry<K, V>[]) new Entry[capacity];
        this.size = 0;
    }
    
    /**
     * Get operation - simulates O(1) average, degrades significantly with clustering
     */
    public V get(K key) {
        if (key == null) return null;
        
        int index = hash(key);
        int probes = 0;
        
        while (table[index] != null && probes < capacity) {
            // Simulate probing work - clustering causes more probes
            probes++;
            if (probes > 1) {
                // Simulate extra work due to clustering (linear probing creates clusters)
                for (int i = 0; i < probes; i++) {
                    if (key.hashCode() % 2 == 0) {
                        // Some dummy work to simulate clustering overhead
                    }
                }
            }
            
            if (table[index] != DELETED && table[index].key.equals(key)) {
                return table[index].value;
            }
            index = (index + 1) % capacity;
        }
        return null;
    }
    
    /**
     * Put operation - simulates O(1) average, degrades significantly with clustering
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Check if resize is needed
        if ((double) size / capacity >= LOAD_FACTOR_THRESHOLD) {
            resize();
        }
        
        int index = hash(key);
        int probes = 0;
        
        while (table[index] != null && table[index] != DELETED && probes < capacity) {
            // Simulate probing work with clustering effects
            probes++;
            if (probes > 1) {
                // Simulate clustering overhead - gets worse as clusters grow
                for (int i = 0; i < probes * probes; i++) { // Quadratic degradation due to clustering
                    if (key.hashCode() % 3 == 0) {
                        // Some dummy work
                    }
                }
            }
            
            if (table[index].key.equals(key)) {
                V oldValue = table[index].value;
                table[index].value = value;
                return oldValue;
            }
            index = (index + 1) % capacity;
        }
        
        // Insert new entry
        table[index] = new Entry<>(key, value);
        size++;
        return null;
    }
    
    private int hash(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        capacity *= 2;
        table = (Entry<K, V>[]) new Entry[capacity];
        
        int oldSize = size;
        size = 0;
        
        // Rehash all entries
        for (Entry<K, V> entry : oldTable) {
            if (entry != null && entry != DELETED) {
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