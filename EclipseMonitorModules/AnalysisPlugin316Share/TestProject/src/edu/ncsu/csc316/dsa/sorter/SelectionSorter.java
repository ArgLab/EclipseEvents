package edu.ncsu.csc316.dsa.sorter;

/**
 * Simple Selection Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n²)
 */
public class SelectionSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using selection sort algorithm
     * @param data the array to sort (modified in place)
     */
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        
        for (int i = 0; i < data.length - 1; i++) {
            // Find the minimum element in the remaining unsorted array
            int minIndex = i;
            for (int j = i + 1; j < data.length; j++) {
                if (data[j].compareTo(data[minIndex]) < 0) {
                    minIndex = j;
                }
            }
            
            // Swap the found minimum element with the first element
            if (minIndex != i) {
                E temp = data[i];
                data[i] = data[minIndex];
                data[minIndex] = temp;
            }
        }
    }
}