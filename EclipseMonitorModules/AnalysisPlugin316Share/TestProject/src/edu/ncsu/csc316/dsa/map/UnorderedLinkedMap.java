package edu.ncsu.csc316.dsa.map;

/**
 * Unordered linked map implementation for testing the Runtime Analysis Plugin
 * Shows linear O(n) performance that degrades with map size
 * Student-level implementation
 */
public class UnorderedLinkedMap<K, V> {
    
    /**
     * Entry class to store key-value pairs
     */
    private class Entry {
        K key;
        V value;
        Entry next;
        
        public Entry(K key, V value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    
    private Entry head;
    private int size;
    
    /**
     * Creates a new empty UnorderedLinkedMap
     */
    public UnorderedLinkedMap() {
        head = null;
        size = 0;
    }
    
    /**
     * Gets the value associated with the given key
     * Performance is O(n) - linear search through linked list
     * Gets worse as map size increases
     * @param key the key to search for
     * @return the associated value, or null if not found
     */
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Linear search through the linked list (this is the expensive part)
        Entry current = head;
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        
        return null; // Key not found
    }
    
    /**
     * Puts a key-value pair into the map
     * Performance is O(n) - need to search for existing key first
     * @param key the key
     * @param value the value
     * @return the previous value, or null if key was new
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        // Check if key already exists (linear search - expensive!)
        Entry current = head;
        while (current != null) {
            if (current.key.equals(key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }
        
        // Key doesn't exist, add new entry at head
        head = new Entry(key, value, head);
        size++;
        return null;
    }
    
    /**
     * Removes the entry with the given key
     * Performance is O(n) - need to search for key
     * @param key the key to remove
     * @return the removed value, or null if not found
     */
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        if (head == null) {
            return null;
        }
        
        // Check if first entry matches
        if (head.key.equals(key)) {
            V value = head.value;
            head = head.next;
            size--;
            return value;
        }
        
        // Search for key in rest of list
        Entry current = head;
        while (current.next != null) {
            if (current.next.key.equals(key)) {
                V value = current.next.value;
                current.next = current.next.next;
                size--;
                return value;
            }
            current = current.next;
        }
        
        return null; // Key not found
    }
    
    /**
     * Returns the current size of the map
     * @return the size
     */
    public int size() {
        return size;
    }
    
    /**
     * Checks if the map is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
}