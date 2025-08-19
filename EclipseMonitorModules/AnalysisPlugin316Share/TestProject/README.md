# Runtime Analysis Test Project

This project contains sample sorting algorithm implementations for testing the Runtime Analysis Plugin.

## Included Algorithms

### Workshop 1: Sorting Algorithms
- **BubbleSorter** - O(n²) complexity with early termination optimization
- **SelectionSorter** - O(n²) complexity, consistent performance
- **InsertionSorter** - O(n²) worst case, O(n) best case for nearly sorted data
- **MergeSorter** - O(n log n) divide-and-conquer algorithm

## Testing Instructions

1. **Import this project** into your Eclipse workspace:
   - File → Import → General → Existing Projects into Workspace
   - Browse to this folder and select it
   - Click Finish

2. **Ensure the project compiles** without errors:
   - Check that all `.java` files have no red error markers
   - Right-click project → Refresh if needed

3. **Test the Runtime Analysis Plugin**:
   - Go to Analysis menu → Run Runtime Analysis
   - Select "Workshop 1: Sorting" category
   - Choose an algorithm (e.g., "Bubble Sort")
   - Select input sizes (recommended: 100, 500, 1000, 2000, 5000)
   - Click OK to run analysis

## Expected Results

- **Bubble Sort & Selection Sort**: Should show O(n²) growth pattern
- **Insertion Sort**: Should show O(n²) for random data, faster for nearly sorted data
- **Merge Sort**: Should show consistent O(n log n) growth pattern

## Troubleshooting

If analysis fails:
- Ensure all files compile without errors
- Check that package names match exactly: `edu.ncsu.csc316.dsa.sorter`
- Verify method signatures match: `public void sort(E[] data)`

## Compilation Issues Testing

To test the plugin's compilation error handling:
1. Introduce a syntax error (e.g., remove a semicolon)
2. Try to run analysis - you should get a helpful error message
3. Fix the error and try again