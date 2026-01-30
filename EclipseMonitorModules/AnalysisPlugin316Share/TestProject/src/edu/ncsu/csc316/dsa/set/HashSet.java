package edu.ncsu.csc316.dsa.set;

/**
 * A HashSet implements a set using a hash table.
 * This implementation simulates collision-dependent performance.
 */
public class HashSet<E> {
    
    private Node<E>[] table;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 17;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;
    
    @SuppressWarnings("unchecked")
    public HashSet() {
        this(DEFAULT_CAPACITY);
    }
    
    @SuppressWarnings("unchecked")
    public HashSet(int capacity) {
        this.capacity = capacity;
        this.table = (Node<E>[]) new Node[capacity];
        this.size = 0;
    }
    
    /**
     * Add operation - simulates O(1) average performance, degrades with collisions
     */
    public void add(E value) {
        if (value == null) return;
        
        // Check if resize is needed
        if ((double) size / capacity >= LOAD_FACTOR_THRESHOLD) {
            resize();
        }
        
        int index = hash(value);
        Node<E> current = table[index];
        
        // Simulate work proportional to collision chain length
        int chainLength = 0;
        Node<E> temp = current;
        while (temp != null) {
            chainLength++;
            temp = temp.next;
        }
        
        // Simulate collision resolution work
        for (int i = 0; i < chainLength; i++) {
            if (value.hashCode() % 2 == 0) {
                // Some dummy work to simulate collision handling
            }
        }
        
        // Check if value already exists
        while (current != null) {
            if (current.value.equals(value)) {
                return; // Already exists
            }
            current = current.next;
        }
        
        // Add new value
        Node<E> newNode = new Node<>(value);
        newNode.next = table[index];
        table[index] = newNode;
        size++;
    }
    
    /**
     * Contains operation - simulates O(1) average performance, degrades with collisions
     */
    public boolean contains(E value) {
        if (value == null) return false;
        
        int index = hash(value);
        Node<E> current = table[index];
        
        // Simulate work proportional to collision chain length
        int chainLength = 0;
        Node<E> temp = current;
        while (temp != null) {
            chainLength++;
            temp = temp.next;
        }
        
        // Simulate collision resolution work
        for (int i = 0; i < chainLength; i++) {
            if (value.hashCode() % 3 == 0) {
                // Some dummy work to simulate collision handling
            }
        }
        
        // Actually search for the value
        while (current != null) {
            if (current.value.equals(value)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
    
    /**
     * Remove operation - simulates O(1) average performance, degrades with collisions
     */
    public boolean remove(E value) {
        if (value == null) return false;
        
        int index = hash(value);
        Node<E> current = table[index];
        Node<E> previous = null;
        
        // Simulate work proportional to collision chain length
        int chainLength = 0;
        Node<E> temp = current;
        while (temp != null) {
            chainLength++;
            temp = temp.next;
        }
        
        // Simulate collision resolution work
        for (int i = 0; i < chainLength; i++) {
            if (value.hashCode() % 4 == 0) {
                // Some dummy work to simulate collision handling
            }
        }
        
        // Search for and remove the value
        while (current != null) {
            if (current.value.equals(value)) {
                if (previous == null) {
                    table[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }
    
    private int hash(E value) {
        return Math.abs(value.hashCode()) % capacity;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] oldTable = table;
        capacity *= 2;
        table = (Node<E>[]) new Node[capacity];
        
        int oldSize = size;
        size = 0;
        
        // Rehash all values
        for (Node<E> head : oldTable) {
            Node<E> current = head;
            while (current != null) {
                add(current.value);
                current = current.next;
            }
        }
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Node<E> {
        E value;
        Node<E> next;
        
        public Node(E value) {
            this.value = value;
            this.next = null;
        }
    }
}