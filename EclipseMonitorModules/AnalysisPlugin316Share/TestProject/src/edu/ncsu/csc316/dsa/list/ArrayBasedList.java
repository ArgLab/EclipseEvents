package edu.ncsu.csc316.dsa.list;

/**
 * Array-based list implementation for testing the Runtime Analysis Plugin
 * Shows position-based performance characteristics 
 * Student-level implementation
 */
public class ArrayBasedList<E> {
    
    private static final int DEFAULT_CAPACITY = 10;
    private E[] data;
    private int size;
    
    /**
     * Creates a new ArrayBasedList with default capacity
     */
    @SuppressWarnings("unchecked")
    public ArrayBasedList() {
        data = (E[]) new Object[DEFAULT_CAPACITY];
        size = 0;
    }
    
    /**
     * Adds element at the specified index
     * Performance depends on position - O(n) at beginning, O(1) at end
     * @param index the position to add at
     * @param element the element to add
     */
    public void add(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        // Check if we need to resize
        if (size >= data.length) {
            resize();
        }
        
        // Shift elements to the right (this is the expensive part)
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        
        data[index] = element;
        size++;
    }
    
    /**
     * Gets element at the specified index
     * Should be constant time O(1)
     * @param index the position to get from
     * @return the element at that position
     */
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return data[index];
    }
    
    /**
     * Removes element at the specified index
     * Performance depends on position - O(n) at beginning, O(1) at end
     * @param index the position to remove from
     * @return the removed element
     */
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        
        E removed = data[index];
        
        // Shift elements to the left (this is the expensive part)
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        
        data[size - 1] = null; // avoid memory leak
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
        add(0, element);
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
        return data[0];
    }
    
    /**
     * Gets the last element
     * @return the last element
     */
    public E last() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return data[size - 1];
    }
    
    /**
     * Sets element at the specified index
     * @param index the position to set
     * @param element the new element
     * @return the old element
     */
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        E oldElement = data[index];
        data[index] = element;
        return oldElement;
    }
    
    /**
     * Removes the first element
     * @return the removed element
     */
    public E removeFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return remove(0);
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
    
    /**
     * Doubles the capacity of the array
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        E[] newData = (E[]) new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }
}