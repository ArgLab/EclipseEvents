package edu.ncsu.csc316.dsa.sorter;

/**
 * Quick Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n log n) average, O(n²) worst case
 * Student-level implementation
 */
public class QuickSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using quick sort algorithm
     * @param data the array to sort (modified in place)
     */
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        quickSort(data, 0, data.length - 1);
    }
    
    /**
     * Recursive quicksort method
     */
    private void quickSort(E[] data, int low, int high) {
        if (low < high) {
            // Find partitioning index
            int pi = partition(data, low, high);
            
            // Recursively sort elements before and after partition
            quickSort(data, low, pi - 1);
            quickSort(data, pi + 1, high);
        }
    }
    
    /**
     * This method takes last element as pivot, places the pivot element 
     * at its correct position in sorted array, and places all smaller 
     * (smaller than pivot) to left of pivot and all greater elements to right of pivot
     */
    private int partition(E[] data, int low, int high) {
        // Choose rightmost element as pivot
        E pivot = data[high];
        
        // Index of smaller element and indicates right position of pivot found so far
        int i = (low - 1);
        
        for (int j = low; j <= high - 1; j++) {
            // If current element is smaller than or equal to pivot
            if (data[j].compareTo(pivot) <= 0) {
                i++; // increment index of smaller element
                swap(data, i, j);
            }
        }
        swap(data, i + 1, high);
        return (i + 1);
    }
    
    /**
     * Swap two elements in the array
     */
    private void swap(E[] data, int i, int j) {
        E temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }
}