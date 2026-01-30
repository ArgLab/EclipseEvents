package edu.ncsu.csc316.dsa.stack;

/**
 * Linked stack implementation for testing the Runtime Analysis Plugin
 * Shows constant time O(1) operations regardless of stack size
 * Student-level implementation
 */
public class LinkedStack<E> {
    
    /**
     * Node class for the linked stack
     */
    private class Node {
        E data;
        Node next;
        
        public Node(E data, Node next) {
            this.data = data;
            this.next = next;
        }
    }
    
    private Node top;
    private int size;
    
    /**
     * Creates a new empty LinkedStack
     */
    public LinkedStack() {
        top = null;
        size = 0;
    }
    
    /**
     * Pushes an element onto the top of the stack
     * Should be constant time O(1) regardless of stack size
     * @param element the element to push
     */
    public void push(E element) {
        // Create new node and link it to current top
        // This is always O(1) - no traversal needed!
        Node newNode = new Node(element, top);
        top = newNode;
        size++;
    }
    
    /**
     * Pops an element from the top of the stack
     * Should be constant time O(1) regardless of stack size
     * @return the popped element
     */
    public E pop() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        
        // Remove top node and update top pointer
        // This is always O(1) - no traversal needed!
        E element = top.data;
        top = top.next;
        size--;
        
        return element;
    }
    
    /**
     * Returns the top element without removing it
     * Should be constant time O(1) regardless of stack size
     * @return the top element
     */
    public E top() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return top.data;
    }
    
    /**
     * Checks if the stack is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the current size of the stack
     * @return the size
     */
    public int size() {
        return size;
    }
}