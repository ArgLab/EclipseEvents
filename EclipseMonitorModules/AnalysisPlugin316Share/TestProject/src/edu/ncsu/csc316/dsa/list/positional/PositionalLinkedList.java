package edu.ncsu.csc316.dsa.list.positional;

/**
 * Positional linked list implementation for testing the Runtime Analysis Plugin
 * Shows constant time operations for position-based access
 * Student-level implementation
 */
public class PositionalLinkedList<E> {
    
    /**
     * Node class that implements Position
     */
    private class Node implements Position<E> {
        E element;
        Node prev;
        Node next;
        
        public Node(E element, Node prev, Node next) {
            this.element = element;
            this.prev = prev;
            this.next = next;
        }
        
        @Override
        public E getElement() {
            return element;
        }
    }
    
    private Node header;  // sentinel
    private Node trailer; // sentinel
    private int size;
    
    /**
     * Creates a new empty PositionalLinkedList
     */
    public PositionalLinkedList() {
        header = new Node(null, null, null);
        trailer = new Node(null, header, null);
        header.next = trailer;
        size = 0;
    }
    
    /**
     * Adds element at the first position
     * Should be constant time O(1) regardless of list size
     * @param element the element to add
     * @return the position of the new element
     */
    public Position<E> addFirst(E element) {
        return addBetween(element, header, header.next);
    }
    
    /**
     * Adds element at the last position
     * Should be constant time O(1) regardless of list size
     * @param element the element to add
     * @return the position of the new element
     */
    public Position<E> addLast(E element) {
        return addBetween(element, trailer.prev, trailer);
    }
    
    /**
     * Helper method to add element between two nodes
     * This is where the constant time magic happens - no traversal needed!
     */
    private Position<E> addBetween(E element, Node prev, Node next) {
        Node newNode = new Node(element, prev, next);
        prev.next = newNode;
        next.prev = newNode;
        size++;
        return newNode;
    }
    
    /**
     * Removes the element at the given position
     * Should be constant time O(1) regardless of list size
     * @param p the position to remove
     * @return the removed element
     */
    public E remove(Position<E> p) {
        Node node = (Node) p;
        Node prev = node.prev;
        Node next = node.next;
        
        prev.next = next;
        next.prev = prev;
        size--;
        
        E element = node.element;
        node.element = null; // help garbage collection
        node.prev = null;
        node.next = null;
        
        return element;
    }
    
    /**
     * Returns the current size of the list
     * @return the size
     */
    public int size() {
        return size;
    }
    
    /**
     * Checks if the list is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the first position (or null if empty)
     * @return the first position
     */
    public Position<E> first() {
        if (size == 0) return null;
        return header.next;
    }
    
    /**
     * Returns the last position (or null if empty)
     * @return the last position
     */
    public Position<E> last() {
        if (size == 0) return null;
        return trailer.prev;
    }
}