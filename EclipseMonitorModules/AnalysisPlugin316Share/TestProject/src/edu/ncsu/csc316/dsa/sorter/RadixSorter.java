package edu.ncsu.csc316.dsa.sorter;

/**
 * Radix Sort implementation for testing the Runtime Analysis Plugin
 * Expected complexity: O(d × n) where d is the number of digits
 * Student-level implementation
 */
public class RadixSorter<E extends Comparable<E>> {
    
    /**
     * Sorts the given array using radix sort algorithm
     * Assumes elements are Integer objects for simplicity
     * @param data the array to sort (modified in place)
     */
    @SuppressWarnings("unchecked")
    public void sort(E[] data) {
        if (data == null || data.length <= 1) {
            return;
        }
        
        // Find the maximum number to know number of digits
        int max = ((Integer) data[0]).intValue();
        for (int i = 1; i < data.length; i++) {
            int value = ((Integer) data[i]).intValue();
            if (value > max) {
                max = value;
            }
        }
        
        // Do counting sort for every digit
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countingSort(data, exp);
        }
    }
    
    /**
     * A function to do counting sort of data[] according to the digit represented by exp
     */
    @SuppressWarnings("unchecked")
    private void countingSort(E[] data, int exp) {
        int n = data.length;
        E[] output = (E[]) new Comparable[n];
        int[] count = new int[10];
        
        // Store count of occurrences in count[]
        for (int i = 0; i < n; i++) {
            int digit = (((Integer) data[i]).intValue() / exp) % 10;
            count[digit]++;
        }
        
        // Change count[i] so that count[i] now contains actual position of this digit in output[]
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        
        // Build the output array
        for (int i = n - 1; i >= 0; i--) {
            int digit = (((Integer) data[i]).intValue() / exp) % 10;
            output[count[digit] - 1] = data[i];
            count[digit]--;
        }
        
        // Copy the output array to data[], so that data now contains sorted numbers according to current digit
        for (int i = 0; i < n; i++) {
            data[i] = output[i];
        }
    }
}