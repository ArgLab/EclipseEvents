package edu.ncsu.csc316.dsa.sorter;

/**
 * Simple Bubble Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n²)
 */
public class BubbleSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using bubble sort algorithm
     * @param data the array to sort (modified in place)
     */
    public void sort(E[] data) {
    	//int[] arr = new int[20];
        if (data == null || data.length <= 1) {
            return;
        }
        
        boolean swapped;
        for (int i = 0; i < data.length - 1; i++) {
            swapped = false;
            for (int j = 0; j < data.length - 1 - i; j++) {
                if (data[j].compareTo(data[j + 1]) > 0) {
                    // Swap elements
                    E temp = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = temp;
                    swapped = true;
                }
            }
            // If no swapping occurred, array is already sorted
            if (!swapped) {
                break;
            }
        }
    }
}