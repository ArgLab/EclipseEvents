package edu.ncsu.csc316.dsa.sorter;

/**
 * Counting Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n + k) where k is the range of input
 * Student-level implementation
 */
public class CountingSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using counting sort algorithm
     * Assumes elements are Integer objects for simplicity
     * @param data the array to sort (modified in place)
     */
    @SuppressWarnings("unchecked")
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        
        // Find min and max values to determine range
        int min = ((Integer) data[0]).intValue();
        int max = ((Integer) data[0]).intValue();
        
        for (int i = 1; i < data.length; i++) {
            int value = ((Integer) data[i]).intValue();
            if (value < min) min = value;
            if (value > max) max = value;
        }
        
        // Create counting array
        int range = max - min + 1;
        int[] count = new int[range];
        
        // Count occurrences
        for (int i = 0; i < data.length; i++) {
            int value = ((Integer) data[i]).intValue();
            count[value - min]++;
        }
        
        // Reconstruct sorted array
        int index = 0;
        for (int i = 0; i < range; i++) {
            while (count[i] > 0) {
                data[index] = (E) Integer.valueOf(i + min);
                index++;
                count[i]--;
            }
        }
    }
}