package edu.ncsu.csc316.dsa.map.search_tree;

/**
 * A BinarySearchTreeMap implements a map using a binary search tree.
 * This is a dummy implementation that can exhibit O(log n) to O(n) performance depending on balance.
 */
public class BinarySearchTreeMap<K extends Comparable<K>, V> {
    
    private Node<K, V> root;
    private int size;
    
    public BinarySearchTreeMap() {
        root = null;
        size = 0;
    }
    
    /**
     * Get operation - simulates O(log n) best case, O(n) worst case
     */
    public V get(K key) {
        if (key == null) return null;
        
        // Simulate tree traversal - work proportional to tree height
        // In worst case (unbalanced), this could be O(n)
        int depth = getSimulatedDepth();
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and pointer following
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate comparison
            }
        }
        
        return getHelper(root, key);
    }
    
    /**
     * Put operation - simulates O(log n) best case, O(n) worst case
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Simulate tree traversal and insertion work
        int depth = getSimulatedDepth();
        for (int i = 0; i < depth; i++) {
            // Simulate the work of finding insertion point
            if (key.hashCode() % 3 == 0) {
                // Some dummy work
            }
        }
        
        if (root == null) {
            root = new Node<>(key, value);
            size++;
            return null;
        }
        
        return putHelper(root, key, value);
    }
    
    private V getHelper(Node<K, V> node, K key) {
        if (node == null) return null;
        
        int comp = key.compareTo(node.key);
        if (comp == 0) {
            return node.value;
        } else if (comp < 0) {
            return getHelper(node.left, key);
        } else {
            return getHelper(node.right, key);
        }
    }
    
    private V putHelper(Node<K, V> node, K key, V value) {
        int comp = key.compareTo(node.key);
        if (comp == 0) {
            V oldValue = node.value;
            node.value = value;
            return oldValue;
        } else if (comp < 0) {
            if (node.left == null) {
                node.left = new Node<>(key, value);
                size++;
                return null;
            } else {
                return putHelper(node.left, key, value);
            }
        } else {
            if (node.right == null) {
                node.right = new Node<>(key, value);
                size++;
                return null;
            } else {
                return putHelper(node.right, key, value);
            }
        }
    }
    
    private int getSimulatedDepth() {
        if (size == 0) return 1;
        // Simulate potentially unbalanced tree - could degrade to linear
        // Use size-dependent depth that can range from log(n) to n
        return Math.min(size, (int) Math.ceil(Math.log(size) / Math.log(2)) + size / 10);
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
        Node<K, V> left;
        Node<K, V> right;
        
        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }
}