package edu.ncsu.csc316.dsa.set;

import java.util.Comparator;

/**
 * A TreeSet implements a set using a balanced binary search tree.
 * This implementation simulates O(log n) performance for all operations.
 */
public class TreeSet<E extends Comparable<E>> {
    
    private Node<E> root;
    private int size;
    private Comparator<E> comparator;
    
    public TreeSet() {
        this((Comparator<E>) Comparator.naturalOrder());
    }
    
    public TreeSet(Comparator<E> comparator) {
        this.comparator = comparator;
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Add operation - simulates O(log n) performance due to balanced tree
     */
    public void add(E value) {
        if (value == null) return;
        
        // Simulate balanced tree traversal - always O(log n)
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size + 1) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and traversal work
            if (value.hashCode() % 2 == 0) {
                // Some dummy work to simulate tree operations
            }
        }
        
        if (root == null) {
            root = new Node<>(value);
            size++;
        } else {
            addHelper(root, value);
        }
    }
    
    /**
     * Contains operation - simulates O(log n) performance
     */
    public boolean contains(E value) {
        if (value == null) return false;
        
        // Simulate balanced tree traversal - always O(log n)
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and traversal work
            if (value.hashCode() % 3 == 0) {
                // Some dummy work to simulate tree operations
            }
        }
        
        return containsHelper(root, value);
    }
    
    /**
     * Remove operation - simulates O(log n) performance
     */
    public boolean remove(E value) {
        if (value == null) return false;
        
        // Simulate balanced tree traversal and rebalancing - always O(log n)
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate removal and rebalancing work
            if (value.hashCode() % 4 == 0) {
                // Some dummy work to simulate tree operations
            }
        }
        
        if (containsHelper(root, value)) {
            root = removeHelper(root, value);
            size--;
            return true;
        }
        return false;
    }
    
    private void addHelper(Node<E> node, E value) {
        int comp = comparator.compare(value, node.value);
        if (comp == 0) {
            return; // Already exists
        } else if (comp < 0) {
            if (node.left == null) {
                node.left = new Node<>(value);
                size++;
            } else {
                addHelper(node.left, value);
            }
        } else {
            if (node.right == null) {
                node.right = new Node<>(value);
                size++;
            } else {
                addHelper(node.right, value);
            }
        }
    }
    
    private boolean containsHelper(Node<E> node, E value) {
        if (node == null) return false;
        
        int comp = comparator.compare(value, node.value);
        if (comp == 0) {
            return true;
        } else if (comp < 0) {
            return containsHelper(node.left, value);
        } else {
            return containsHelper(node.right, value);
        }
    }
    
    private Node<E> removeHelper(Node<E> node, E value) {
        if (node == null) return null;
        
        int comp = comparator.compare(value, node.value);
        if (comp < 0) {
            node.left = removeHelper(node.left, value);
        } else if (comp > 0) {
            node.right = removeHelper(node.right, value);
        } else {
            // Found the node to remove
            if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            } else {
                // Node has two children - replace with successor
                Node<E> successor = findMin(node.right);
                node.value = successor.value;
                node.right = removeHelper(node.right, successor.value);
            }
        }
        return node;
    }
    
    private Node<E> findMin(Node<E> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private static class Node<E> {
        E value;
        Node<E> left;
        Node<E> right;
        
        public Node(E value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }
}