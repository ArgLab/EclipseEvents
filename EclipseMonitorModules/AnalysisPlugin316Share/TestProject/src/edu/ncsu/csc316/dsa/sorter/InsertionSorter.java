package edu.ncsu.csc316.dsa.sorter;

/**
 * Simple Insertion Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n²) worst case, O(n) best case
 */
public class InsertionSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using insertion sort algorithm
     * @param data the array to sort (modified in place)
     */
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        
        for (int i = 1; i < data.length; i++) {
            E key = data[i];
            int j = i - 1;
            
            // Move elements of data[0..i-1] that are greater than key
            // one position ahead of their current position
            while (j >= 0 && data[j].compareTo(key) > 0) {
                data[j + 1] = data[j];
                j--;
            }
            data[j + 1] = key;
        }
    }
}