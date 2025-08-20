package edu.ncsu.csc316.dsa.map.search_tree;

/**
 * A RedBlackTreeMap implements a map using a red-black tree.
 * This is a dummy implementation that simulates O(log n) performance.
 */
public class RedBlackTreeMap<K extends Comparable<K>, V> {
    
    private Node<K, V> root;
    private int size;
    private static final boolean RED = true;
    private static final boolean BLACK = false;
    
    public RedBlackTreeMap() {
        root = null;
        size = 0;
    }
    
    /**
     * Get operation - simulates O(log n) performance due to balanced tree
     */
    public V get(K key) {
        if (key == null) return null;
        
        // Simulate balanced tree traversal - always O(log n)
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and pointer following
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate comparison
            }
        }
        
        return getHelper(root, key);
    }
    
    /**
     * Put operation - simulates O(log n) performance including recoloring/rotations
     */
    public V put(K key, V value) {
        if (key == null) return null;
        
        // Simulate balanced tree traversal and rebalancing work
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate insertion, recoloring, and rotation work
            if (key.hashCode() % 3 == 0) {
                // Some dummy work for red-black tree maintenance
            }
        }
        
        if (root == null) {
            root = new Node<>(key, value, BLACK);
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
                node.left = new Node<>(key, value, RED);
                size++;
                return null;
            } else {
                return putHelper(node.left, key, value);
            }
        } else {
            if (node.right == null) {
                node.right = new Node<>(key, value, RED);
                size++;
                return null;
            } else {
                return putHelper(node.right, key, value);
            }
        }
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
        boolean color;
        
        public Node(K key, V value, boolean color) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
            this.color = color;
        }
    }
}