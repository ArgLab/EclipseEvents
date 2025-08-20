package edu.ncsu.csc316.dsa.list;

/**
 * Singly linked list implementation for testing the Runtime Analysis Plugin
 * Shows linear position-based performance characteristics
 * Student-level implementation
 */
public class SinglyLinkedList<E> {
    
    /**
     * Node class for the linked list
     */
    private class Node {
        E data;
        Node next;
        
        public Node(E data) {
            this.data = data;
            this.next = null;
        }
    }
    
    private Node head;
    private int size;
    
    /**
     * Creates a new empty SinglyLinkedList
     */
    public SinglyLinkedList() {
        head = null;
        size = 0;
    }
    
    /**
     * Adds element at the specified index
     * Performance is O(n) - linear with position since we need to traverse
     * @param index the position to add at
     * @param element the element to add
     */
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        Node newNode = new Node(element);
        
        if (index == 0) {
            newNode.next = head;
            head = newNode;
        } else {
            // Traverse to position index-1 (this is the expensive part)
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }
        
        size++;
    }
    
    /**
     * Gets element at the specified index
     * Performance is O(n) - linear with position since we need to traverse
     * @param index the position to get from
     * @return the element at that position
     */
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        // Traverse to the specified index (this is the expensive part)
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        
        return current.data;
    }
    
    /**
     * Removes element at the specified index
     * Performance is O(n) - linear with position since we need to traverse
     * @param index the position to remove from
     * @return the removed element
     */
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        E removed;
        
        if (index == 0) {
            removed = head.data;
            head = head.next;
        } else {
            // Traverse to position index-1 (this is the expensive part)
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            removed = current.next.data;
            current.next = current.next.next;
        }
        
        size--;
        return removed;
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
     * Adds element at the beginning of the list
     * @param element the element to add
     */
    public void addFirst(E element) {
        Node newNode = new Node(element);
        newNode.next = head;
        head = newNode;
        size++;
    }
    
    /**
     * Adds element at the end of the list
     * @param element the element to add
     */
    public void addLast(E element) {
        add(size, element);
    }
    
    /**
     * Gets the first element
     * @return the first element
     */
    public E first() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return head.data;
    }
    
    /**
     * Gets the last element
     * @return the last element
     */
    public E last() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        Node current = head;
        while (current.next != null) {
            current = current.next;
        }
        return current.data;
    }
    
    /**
     * Removes the first element
     * @return the removed element
     */
    public E removeFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        E data = head.data;
        head = head.next;
        size--;
        return data;
    }
    
    /**
     * Removes the last element
     * @return the removed element
     */
    public E removeLast() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return remove(size - 1);
    }
}