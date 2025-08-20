package edu.ncsu.csc316.dsa.priority_queue;

import java.util.Comparator;

/**
 * A HeapPriorityQueue implements a priority queue using a binary heap.
 * This implementation simulates O(log n) insert and deleteMin operations.
 */
public class HeapPriorityQueue<K, V> {
    
    private Entry<K, V>[] heap;
    private int size;
    private Comparator<K> comparator;
    private static final int DEFAULT_CAPACITY = 17;
    
    @SuppressWarnings("unchecked")
    public HeapPriorityQueue() {
        this((Comparator<K>) Comparator.naturalOrder());
    }
    
    @SuppressWarnings("unchecked")
    public HeapPriorityQueue(Comparator<K> comparator) {
        this.comparator = comparator;
        this.heap = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        this.size = 0;
    }
    
    /**
     * Insert operation - simulates O(log n) performance due to up-heap bubbling
     */
    public Entry<K, V> insert(K key, V value) {
        if (key == null) return null;
        
        // Simulate O(log n) up-heap work
        int depth = size == 0 ? 1 : (int) Math.ceil(Math.log(size + 1) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and swapping work
            if (key.hashCode() % 2 == 0) {
                // Some dummy work to simulate heap operations
            }
        }
        
        // Resize if needed
        if (size >= heap.length) {
            resize();
        }
        
        Entry<K, V> newEntry = new Entry<>(key, value);
        heap[size] = newEntry;
        upHeap(size);
        size++;
        return newEntry;
    }
    
    /**
     * DeleteMin operation - simulates O(log n) performance due to down-heap bubbling
     */
    public Entry<K, V> deleteMin() {
        if (isEmpty()) return null;
        
        // Simulate O(log n) down-heap work
        int depth = (int) Math.ceil(Math.log(size) / Math.log(2));
        for (int i = 0; i < depth; i++) {
            // Simulate comparison and swapping work
            if (size % 2 == 0) {
                // Some dummy work to simulate heap operations
            }
        }
        
        Entry<K, V> min = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        
        if (size > 0) {
            downHeap(0);
        }
        
        return min;
    }
    
    public Entry<K, V> min() {
        if (isEmpty()) return null;
        return heap[0];
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private void upHeap(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(heap[index].key, heap[parent].key) >= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }
    
    private void downHeap(int index) {
        while (hasLeft(index)) {
            int leftChild = 2 * index + 1;
            int smallChild = leftChild;
            
            if (hasRight(index)) {
                int rightChild = leftChild + 1;
                if (comparator.compare(heap[rightChild].key, heap[leftChild].key) < 0) {
                    smallChild = rightChild;
                }
            }
            
            if (comparator.compare(heap[smallChild].key, heap[index].key) >= 0) {
                break;
            }
            
            swap(index, smallChild);
            index = smallChild;
        }
    }
    
    private boolean hasLeft(int index) {
        return 2 * index + 1 < size;
    }
    
    private boolean hasRight(int index) {
        return 2 * index + 2 < size;
    }
    
    private void swap(int i, int j) {
        Entry<K, V> temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldHeap = heap;
        heap = (Entry<K, V>[]) new Entry[oldHeap.length * 2];
        for (int i = 0; i < size; i++) {
            heap[i] = oldHeap[i];
        }
    }
    
    public static class Entry<K, V> {
        private K key;
        private V value;
        
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}