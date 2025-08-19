package edu.ncsu.csc316.dsa.tree;

import edu.ncsu.csc316.dsa.list.positional.Position;

/**
 * A LinkedBinaryTree implements a binary tree using linked nodes.
 * This is a dummy implementation for testing tree operations.
 */
public class LinkedBinaryTree<E> {
    
    private Node<E> root;
    private int size;
    
    public LinkedBinaryTree() {
        root = null;
        size = 0;
    }
    
    /**
     * Add left child - simulates O(1) operation
     */
    public Position<E> addLeft(Position<E> p, E value) {
        Node<E> parent = validate(p);
        if (parent.left != null) {
            throw new IllegalArgumentException("Position already has a left child");
        }
        
        // Simulate O(1) work
        Node<E> newNode = new Node<>(value, parent, null, null);
        parent.left = newNode;
        size++;
        return newNode;
    }
    
    /**
     * Add right child - simulates O(1) operation
     */
    public Position<E> addRight(Position<E> p, E value) {
        Node<E> parent = validate(p);
        if (parent.right != null) {
            throw new IllegalArgumentException("Position already has a right child");
        }
        
        // Simulate O(1) work
        Node<E> newNode = new Node<>(value, parent, null, null);
        parent.right = newNode;
        size++;
        return newNode;
    }
    
    /**
     * Add root - simulates O(1) operation
     */
    public Position<E> addRoot(E value) {
        if (root != null) {
            throw new IllegalArgumentException("Tree already has a root");
        }
        
        root = new Node<>(value, null, null, null);
        size = 1;
        return root;
    }
    
    /**
     * Remove node - simulates O(1) operation for nodes with 0 or 1 child
     */
    public E remove(Position<E> p) {
        Node<E> node = validate(p);
        
        if (node.left != null && node.right != null) {
            throw new IllegalArgumentException("Cannot remove node with two children");
        }
        
        // Simulate O(1) work
        Node<E> child = (node.left != null) ? node.left : node.right;
        
        if (child != null) {
            child.parent = node.parent;
        }
        
        if (node == root) {
            root = child;
        } else {
            Node<E> parent = node.parent;
            if (node == parent.left) {
                parent.left = child;
            } else {
                parent.right = child;
            }
        }
        
        size--;
        E result = node.element;
        node.element = null;
        node.parent = node.left = node.right = null;
        return result;
    }
    
    public Position<E> root() {
        return root;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private Node<E> validate(Position<E> p) {
        if (!(p instanceof Node)) {
            throw new IllegalArgumentException("Invalid position");
        }
        Node<E> node = (Node<E>) p;
        if (node.parent == node) { // Convention for defunct node
            throw new IllegalArgumentException("Position is no longer valid");
        }
        return node;
    }
    
    private static class Node<E> implements Position<E> {
        private E element;
        private Node<E> parent;
        private Node<E> left;
        private Node<E> right;
        
        public Node(E element, Node<E> parent, Node<E> left, Node<E> right) {
            this.element = element;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
        
        @Override
        public E getElement() {
            return element;
        }
    }
}