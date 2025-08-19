package edu.ncsu.csc316.dsa.sorter;

/**
 * Simple Merge Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(n log n)
 */
public class MergeSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using merge sort algorithm
     * @param data the array to sort (modified in place)
     */
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        mergeSort(data, 0, data.length - 1);
    }
    
    /**
     * Recursively sorts the array using merge sort
     * @param data the array to sort
     * @param left the left index
     * @param right the right index
     */
    private void mergeSort(E[] data, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            
            // Sort first and second halves
            mergeSort(data, left, mid);
            mergeSort(data, mid + 1, right);
            
            // Merge the sorted halves
            merge(data, left, mid, right);
        }
    }
    
    /**
     * Merges two sorted subarrays
     * @param data the array containing both subarrays
     * @param left the left index
     * @param mid the middle index
     * @param right the right index
     */
    @SuppressWarnings("unchecked")
    private void merge(E[] data, int left, int mid, int right) {
        // Create temporary arrays for the two subarrays
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        E[] leftArray = (E[]) new Comparable[n1];
        E[] rightArray = (E[]) new Comparable[n2];
        
        // Copy data to temporary arrays
        for (int i = 0; i < n1; i++) {
            leftArray[i] = data[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = data[mid + 1 + j];
        }
        
        // Merge the temporary arrays back into data[left..right]
        int i = 0, j = 0, k = left;
        
        while (i < n1 && j < n2) {
            if (leftArray[i].compareTo(rightArray[j]) <= 0) {
                data[k] = leftArray[i];
                i++;
            } else {
                data[k] = rightArray[j];
                j++;
            }
            k++;
        }
        
        // Copy remaining elements
        while (i < n1) {
            data[k] = leftArray[i];
            i++;
            k++;
        }
        
        while (j < n2) {
            data[k] = rightArray[j];
            j++;
            k++;
        }
    }
}