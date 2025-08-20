package edu.ncsu.csc316.dsa.queue;

/**
 * Array-based queue implementation for testing the Runtime Analysis Plugin
 * Shows constant time O(1) operations with occasional resize spikes
 * Student-level implementation using circular array approach
 */
public class ArrayBasedQueue<E> {
    
    private static final int DEFAULT_CAPACITY = 10;
    private E[] data;
    private int front;
    private int size;
    
    /**
     * Creates a new ArrayBasedQueue with default capacity
     */
    @SuppressWarnings("unchecked")
    public ArrayBasedQueue() {
        data = (E[]) new Object[DEFAULT_CAPACITY];
        front = 0;
        size = 0;
    }
    
    /**
     * Adds an element to the rear of the queue
     * Should be O(1) amortized (with occasional resize cost)
     * @param element the element to enqueue
     */
    public void enqueue(E element) {
        // Check if we need to resize (this occasionally makes it expensive)
        if (size == data.length) {
            resize();
        }
        
        // Calculate rear position using circular array math
        int rear = (front + size) % data.length;
        data[rear] = element;
        size++;
    }
    
    /**
     * Removes and returns element from the front of the queue
     * Should be constant time O(1) regardless of queue size
     * @return the dequeued element
     */
    public E dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        
        E element = data[front];
        data[front] = null; // avoid memory leak
        front = (front + 1) % data.length; // circular array math
        size--;
        
        return element;
    }
    
    /**
     * Returns the front element without removing it
     * Should be constant time O(1) regardless of queue size
     * @return the front element
     */
    public E front() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        return data[front];
    }
    
    /**
     * Checks if the queue is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the current size of the queue
     * @return the size
     */
    public int size() {
        return size;
    }
    
    /**
     * Doubles the capacity of the array and reorganizes elements
     * This is the expensive operation that happens occasionally
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        E[] newData = (E[]) new Object[data.length * 2];
        
        // Copy elements in order from front to rear
        for (int i = 0; i < size; i++) {
            newData[i] = data[(front + i) % data.length];
        }
        
        data = newData;
        front = 0; // reset front to beginning of new array
    }
}