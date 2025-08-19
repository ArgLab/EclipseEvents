// File: AlgorithmRegistry.java
package edu.runtimeanalysis.models;

import java.util.*;

/**
 * Registry of available algorithms for analysis, organized by workshop
 */
public class AlgorithmRegistry {
    private static final Map<String, List<AlgorithmConfig>> ALGORITHMS = new HashMap<>();
    
    static {
        // Workshop 1: Algorithm Analysis & Sorting
        List<AlgorithmConfig> workshop1 = new ArrayList<>();
        
        // Basic sorting algorithms - all use traditional scaling with array sizes
        workshop1.add(new AlgorithmConfig(
            "Bubble Sort",
            "edu.ncsu.csc316.dsa.sorter.BubbleSorter",
            "Workshop 1: Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class, 
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n²)",
            "Generate random integer array of specified size"
        ));
        
        workshop1.add(new AlgorithmConfig(
            "Selection Sort",
            "edu.ncsu.csc316.dsa.sorter.SelectionSorter",
            "Workshop 1: Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n²)",
            "Generate random integer array of specified size"
        ));
        
        workshop1.add(new AlgorithmConfig(
            "Insertion Sort",
            "edu.ncsu.csc316.dsa.sorter.InsertionSorter",
            "Workshop 1: Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n²)",
            "Generate random integer array of specified size"
        ));
        
        workshop1.add(new AlgorithmConfig(
            "Counting Sort",
            "edu.ncsu.csc316.dsa.sorter.CountingSorter",
            "Workshop 1: Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n + k)",
            "Generate random integer array of specified size"
        ));
        
        workshop1.add(new AlgorithmConfig(
            "Radix Sort",
            "edu.ncsu.csc316.dsa.sorter.RadixSorter",
            "Workshop 1: Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(d × n)",
            "Generate random integer array of specified size"
        ));
        
        ALGORITHMS.put("Workshop 1: Sorting", workshop1);
        
        // Workshop 2: List & Positional List ADTs
        List<AlgorithmConfig> workshop2 = new ArrayList<>();
        
        // ArrayBasedList operations - position-based for indexed operations
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.add()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("add", new Class[]{int.class, Object.class}, void.class,
                              "public void add(int index, E element)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(n) at beginning, O(1) at end",
            "Pre-populate list with 1000 elements, test adding at different positions"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.get()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("get", new Class[]{int.class}, Object.class,
                              "public E get(int index)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(1)",
            "Test getting at different positions - should be constant time regardless of position",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.remove()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("remove", new Class[]{int.class}, Object.class,
                              "public E remove(int index)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(n) at beginning, O(1) at end",
            "Pre-populate list with 1000 elements, test removing from different positions"
        ));
        
        // Additional ArrayBasedList methods
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.addFirst()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("addFirst", new Class[]{Object.class}, void.class,
                              "public void addFirst(E element)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n)",
            "Generate array list and add elements to front"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.addLast()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("addLast", new Class[]{Object.class}, void.class,
                              "public void addLast(E element)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(1) amortized",
            "Generate array list and add elements to end"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.first()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("first", new Class[]{}, Object.class,
                              "public E first()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate array list and access first element",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.last()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("last", new Class[]{}, Object.class,
                              "public E last()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate array list and access last element",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.set()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("set", new Class[]{int.class, Object.class}, Object.class,
                              "public E set(int index, E element)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(1)",
            "Generate array list and update elements at random indices"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.removeFirst()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("removeFirst", new Class[]{}, Object.class,
                              "public E removeFirst()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(n)",
            "Generate array list and remove first elements",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.removeLast()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("removeLast", new Class[]{}, Object.class,
                              "public E removeLast()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate array list and remove last elements",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.size()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("size", new Class[]{}, int.class,
                              "public int size()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate array list and check size"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "ArrayBasedList.isEmpty()",
            "edu.ncsu.csc316.dsa.list.ArrayBasedList",
            "Workshop 2: Lists",
            new MethodSignature("isEmpty", new Class[]{}, boolean.class,
                              "public boolean isEmpty()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate array list and check if empty"
        ));
        
        // SinglyLinkedList operations - position-based for indexed operations
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.add()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("add", new Class[]{int.class, Object.class}, void.class,
                              "public void add(int index, E element)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(n) - linear with position",
            "Pre-populate list with 1000 elements, test adding at different positions"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.get()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("get", new Class[]{int.class}, Object.class,
                              "public E get(int index)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(n) - linear with position",
            "Pre-populate list with 1000 elements, test getting from different positions"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.remove()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("remove", new Class[]{int.class}, Object.class,
                              "public E remove(int index)"),
            AnalysisMode.POSITION_BASED,
            new PositionIndexParameter(1000),
            "O(n) - linear with position",
            "Pre-populate list with 1000 elements, test removing from different positions"
        ));
        
        // Additional SinglyLinkedList methods
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.addFirst()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("addFirst", new Class[]{Object.class}, void.class,
                              "public void addFirst(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and add elements to front"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.addLast()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("addLast", new Class[]{Object.class}, void.class,
                              "public void addLast(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and add elements to end"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.first()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("first", new Class[]{}, Object.class,
                              "public E first()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and access first element"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.last()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("last", new Class[]{}, Object.class,
                              "public E last()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and access last element"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.removeFirst()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("removeFirst", new Class[]{}, Object.class,
                              "public E removeFirst()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and remove first elements",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.removeLast()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("removeLast", new Class[]{}, Object.class,
                              "public E removeLast()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(n)",
            "Generate linked list and remove last elements",
            new String[]{"add"}
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.size()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("size", new Class[]{}, int.class,
                              "public int size()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and check size"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "SinglyLinkedList.isEmpty()",
            "edu.ncsu.csc316.dsa.list.SinglyLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("isEmpty", new Class[]{}, boolean.class,
                              "public boolean isEmpty()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate linked list and check if empty"
        ));
        
        // PositionalLinkedList operations - constant time verification
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.addFirst()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("addFirst", new Class[]{Object.class}, Object.class,
                              "public Position<E> addFirst(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test adding first element to lists of different sizes - should be constant time"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.addLast()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("addLast", new Class[]{Object.class}, Object.class,
                              "public Position<E> addLast(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate positional list and add elements to end"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.first()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("first", new Class[]{}, Object.class,
                              "public Position<E> first()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate positional list and access first position"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.last()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("last", new Class[]{}, Object.class,
                              "public Position<E> last()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate positional list and access last position"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.size()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("size", new Class[]{}, int.class,
                              "public int size()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate positional list and check size"
        ));
        
        workshop2.add(new AlgorithmConfig(
            "PositionalLinkedList.isEmpty()",
            "edu.ncsu.csc316.dsa.list.positional.PositionalLinkedList",
            "Workshop 2: Lists",
            new MethodSignature("isEmpty", new Class[]{}, boolean.class,
                              "public boolean isEmpty()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Generate positional list and check if empty"
        ));
        
        ALGORITHMS.put("Workshop 2: Lists", workshop2);
        
        // Workshop 3: Stack & Queue ADTs
        List<AlgorithmConfig> workshop3 = new ArrayList<>();
        
        // Stack operations - should be constant time regardless of stack size
        workshop3.add(new AlgorithmConfig(
            "LinkedStack.push()",
            "edu.ncsu.csc316.dsa.stack.LinkedStack",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("push", new Class[]{Object.class}, void.class,
                              "public void push(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test pushing to stacks with different numbers of existing elements"
        ));
        
        workshop3.add(new AlgorithmConfig(
            "LinkedStack.pop()",
            "edu.ncsu.csc316.dsa.stack.LinkedStack",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("pop", new Class[]{}, Object.class,
                              "public E pop()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test popping from stacks with different numbers of existing elements",
            new String[]{"push"}
        ));
        
        // Queue operations - should be constant time but may have resize spikes
        workshop3.add(new AlgorithmConfig(
            "ArrayBasedQueue.enqueue()",
            "edu.ncsu.csc316.dsa.queue.ArrayBasedQueue",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("enqueue", new Class[]{Object.class}, void.class,
                              "public void enqueue(E element)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1) amortized",
            "Test enqueue to queues with different numbers of existing elements"
        ));
        
        workshop3.add(new AlgorithmConfig(
            "ArrayBasedQueue.dequeue()",
            "edu.ncsu.csc316.dsa.queue.ArrayBasedQueue",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("dequeue", new Class[]{}, Object.class,
                              "public E dequeue()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test dequeue from queues with different numbers of existing elements",
            new String[]{"enqueue"}
        ));
        
        // Additional Stack methods
        workshop3.add(new AlgorithmConfig(
            "LinkedStack.top()",
            "edu.ncsu.csc316.dsa.stack.LinkedStack",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("top", new Class[]{}, Object.class,
                              "public E top()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test accessing top element from stacks with different numbers of existing elements",
            new String[]{"push"}
        ));
        
        workshop3.add(new AlgorithmConfig(
            "LinkedStack.size()",
            "edu.ncsu.csc316.dsa.stack.LinkedStack",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("size", new Class[]{}, int.class,
                              "public int size()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test getting size of stacks with different numbers of existing elements"
        ));
        
        workshop3.add(new AlgorithmConfig(
            "LinkedStack.isEmpty()",
            "edu.ncsu.csc316.dsa.stack.LinkedStack",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("isEmpty", new Class[]{}, boolean.class,
                              "public boolean isEmpty()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test checking if stacks are empty with different numbers of existing elements"
        ));
        
        // Additional Queue methods
        workshop3.add(new AlgorithmConfig(
            "ArrayBasedQueue.front()",
            "edu.ncsu.csc316.dsa.queue.ArrayBasedQueue",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("front", new Class[]{}, Object.class,
                              "public E front()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test accessing front element from queues with different numbers of existing elements",
            new String[]{"enqueue"}
        ));
        
        workshop3.add(new AlgorithmConfig(
            "ArrayBasedQueue.size()",
            "edu.ncsu.csc316.dsa.queue.ArrayBasedQueue",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("size", new Class[]{}, int.class,
                              "public int size()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test getting size of queues with different numbers of existing elements"
        ));
        
        workshop3.add(new AlgorithmConfig(
            "ArrayBasedQueue.isEmpty()",
            "edu.ncsu.csc316.dsa.queue.ArrayBasedQueue",
            "Workshop 3: Stacks & Queues",
            new MethodSignature("isEmpty", new Class[]{}, boolean.class,
                              "public boolean isEmpty()"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test checking if queues are empty with different numbers of existing elements"
        ));
        
        ALGORITHMS.put("Workshop 3: Stacks & Queues", workshop3);
        
        // Workshop 4: Recursion (Advanced Sorting)
        List<AlgorithmConfig> workshop4 = new ArrayList<>();
        
        workshop4.add(new AlgorithmConfig(
            "Merge Sort",
            "edu.ncsu.csc316.dsa.sorter.MergeSorter",
            "Workshop 4: Recursive Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n log n)",
            "Generate random integer array of specified size"
        ));
        
        workshop4.add(new AlgorithmConfig(
            "Quick Sort",
            "edu.ncsu.csc316.dsa.sorter.QuickSorter",
            "Workshop 4: Recursive Sorting",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class,
                              "public void sort(E[] data)"),
            AnalysisMode.TRADITIONAL_SCALING,
            new InputSizeParameter(),
            "O(n log n) average, O(n²) worst",
            "Generate random integer array of specified size"
        ));
        
        ALGORITHMS.put("Workshop 4: Recursive Sorting", workshop4);
        
        // Workshop 5: Map ADT
        List<AlgorithmConfig> workshop5 = new ArrayList<>();
        
        workshop5.add(new AlgorithmConfig(
            "UnorderedLinkedMap.get()",
            "edu.ncsu.csc316.dsa.map.UnorderedLinkedMap",
            "Workshop 5: Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(n)",
            "Pre-populate map with specified number of entries, test get operation"
        ));
        
        workshop5.add(new AlgorithmConfig(
            "UnorderedLinkedMap.put()",
            "edu.ncsu.csc316.dsa.map.UnorderedLinkedMap",
            "Workshop 5: Maps",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(n)",
            "Pre-populate map with specified number of entries, test put operation"
        ));
        
        workshop5.add(new AlgorithmConfig(
            "SearchTableMap.get()",
            "edu.ncsu.csc316.dsa.map.SearchTableMap",
            "Workshop 5: Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate sorted map with specified number of entries, test get operation"
        ));
        
        workshop5.add(new AlgorithmConfig(
            "SearchTableMap.put()",
            "edu.ncsu.csc316.dsa.map.SearchTableMap",
            "Workshop 5: Maps",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(n)",
            "Pre-populate sorted map with specified number of entries, test put operation"
        ));
        
        workshop5.add(new AlgorithmConfig(
            "SkipListMap.get()",
            "edu.ncsu.csc316.dsa.map.SkipListMap",
            "Workshop 5: Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) expected",
            "Pre-populate skip list with specified number of entries, test get operation"
        ));
        
        workshop5.add(new AlgorithmConfig(
            "SkipListMap.put()",
            "edu.ncsu.csc316.dsa.map.SkipListMap",
            "Workshop 5: Maps",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) expected",
            "Pre-populate skip list with specified number of entries, test put operation"
        ));
        
        // Search operations that demonstrate position-dependent performance
        workshop5.add(new AlgorithmConfig(
            "UnorderedLinkedMap.get() - Position Analysis",
            "edu.ncsu.csc316.dsa.map.UnorderedLinkedMap",
            "Workshop 5: Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.INPUT_SIZE_WITH_POSITIONS,
            new InputSizeWithPositionsParameter(),
            "O(n) - linear search, position matters",
            "Test get operation with keys at Beginning, Middle, End positions in different sized maps"
        ));
        
        ALGORITHMS.put("Workshop 5: Maps", workshop5);
        
        // Workshop 6: Tree ADT  
        List<AlgorithmConfig> workshop6 = new ArrayList<>();
        
        workshop6.add(new AlgorithmConfig(
            "LinkedBinaryTree.addLeft()",
            "edu.ncsu.csc316.dsa.tree.LinkedBinaryTree",
            "Workshop 6: Trees",
            new MethodSignature("addLeft", new Class[]{Object.class, Object.class}, Object.class,
                              "public Position<E> addLeft(Position<E> p, E value)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test adding left child to trees with different numbers of existing nodes"
        ));
        
        workshop6.add(new AlgorithmConfig(
            "LinkedBinaryTree.remove()",
            "edu.ncsu.csc316.dsa.tree.LinkedBinaryTree",
            "Workshop 6: Trees",
            new MethodSignature("remove", new Class[]{Object.class}, Object.class,
                              "public E remove(Position<E> p)"),
            AnalysisMode.CONSTANT_TIME,
            new StructureSizeParameter(),
            "O(1)",
            "Test removing nodes from trees with different numbers of existing nodes"
        ));
        
        ALGORITHMS.put("Workshop 6: Trees", workshop6);
        
        // Workshop 7: Search Tree Maps
        List<AlgorithmConfig> workshop7 = new ArrayList<>();
        
        workshop7.add(new AlgorithmConfig(
            "BinarySearchTreeMap.get()",
            "edu.ncsu.csc316.dsa.map.search_tree.BinarySearchTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) best, O(n) worst",
            "Pre-populate BST with specified number of entries, test get operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "BinarySearchTreeMap.put()",
            "edu.ncsu.csc316.dsa.map.search_tree.BinarySearchTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) best, O(n) worst",
            "Pre-populate BST with specified number of entries, test put operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "AVLTreeMap.get()",
            "edu.ncsu.csc316.dsa.map.search_tree.AVLTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate balanced AVL tree with specified number of entries, test get operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "AVLTreeMap.put()",
            "edu.ncsu.csc316.dsa.map.search_tree.AVLTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate balanced AVL tree with specified number of entries, test put operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "SplayTreeMap.get()",
            "edu.ncsu.csc316.dsa.map.search_tree.SplayTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) amortized",
            "Pre-populate splay tree with specified number of entries, test get operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "SplayTreeMap.put()",
            "edu.ncsu.csc316.dsa.map.search_tree.SplayTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) amortized",
            "Pre-populate splay tree with specified number of entries, test put operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "RedBlackTreeMap.get()",
            "edu.ncsu.csc316.dsa.map.search_tree.RedBlackTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate balanced red-black tree with specified number of entries, test get operation"
        ));
        
        workshop7.add(new AlgorithmConfig(
            "RedBlackTreeMap.put()",
            "edu.ncsu.csc316.dsa.map.search_tree.RedBlackTreeMap",
            "Workshop 7: Search Trees",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate balanced red-black tree with specified number of entries, test put operation"
        ));
        
        ALGORITHMS.put("Workshop 7: Search Trees", workshop7);
        
        // Workshop 8: Hash Maps
        List<AlgorithmConfig> workshop8 = new ArrayList<>();
        
        // Hash map operations - analyze performance vs collision scenarios
        workshop8.add(new AlgorithmConfig(
            "SeparateChainingHashMap.get()",
            "edu.ncsu.csc316.dsa.map.hashing.SeparateChainingHashMap",
            "Workshop 8: Hash Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average, degrades with collisions",
            "Test get operation under different collision scenarios: Low, Medium, High"
        ));
        
        workshop8.add(new AlgorithmConfig(
            "SeparateChainingHashMap.put()",
            "edu.ncsu.csc316.dsa.map.hashing.SeparateChainingHashMap",
            "Workshop 8: Hash Maps",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average, degrades with collisions",
            "Test put operation under different collision scenarios: Low, Medium, High"
        ));
        
        workshop8.add(new AlgorithmConfig(
            "LinearProbingHashMap.get()",
            "edu.ncsu.csc316.dsa.map.hashing.LinearProbingHashMap",
            "Workshop 8: Hash Maps",
            new MethodSignature("get", new Class[]{Object.class}, Object.class,
                              "public V get(K key)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average, degrades significantly with clustering",
            "Test get operation under different collision scenarios: Low, Medium, High"
        ));
        
        workshop8.add(new AlgorithmConfig(
            "LinearProbingHashMap.put()",
            "edu.ncsu.csc316.dsa.map.hashing.LinearProbingHashMap",
            "Workshop 8: Hash Maps",
            new MethodSignature("put", new Class[]{Object.class, Object.class}, Object.class,
                              "public V put(K key, V value)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average, degrades significantly with clustering",
            "Test put operation under different collision scenarios: Low, Medium, High"
        ));
        
        ALGORITHMS.put("Workshop 8: Hash Maps", workshop8);
        
        // Workshop 9: Priority Queue ADT
        List<AlgorithmConfig> workshop9 = new ArrayList<>();
        
        workshop9.add(new AlgorithmConfig(
            "HeapPriorityQueue.insert()",
            "edu.ncsu.csc316.dsa.priority_queue.HeapPriorityQueue",
            "Workshop 9: Priority Queues",
            new MethodSignature("insert", new Class[]{Object.class, Object.class}, Object.class,
                              "public Entry<K,V> insert(K key, V value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate priority queue with specified number of entries, test insert operation"
        ));
        
        workshop9.add(new AlgorithmConfig(
            "HeapPriorityQueue.deleteMin()",
            "edu.ncsu.csc316.dsa.priority_queue.HeapPriorityQueue",
            "Workshop 9: Priority Queues",
            new MethodSignature("deleteMin", new Class[]{}, Object.class,
                              "public Entry<K,V> deleteMin()"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate priority queue with specified number of entries, test deleteMin operation"
        ));
        
        // Note: HeapAdaptablePriorityQueue inherits insert() from HeapPriorityQueue
        // Using HeapPriorityQueue.insert() instead to avoid inheritance scanning issues
        
        workshop9.add(new AlgorithmConfig(
            "HeapAdaptablePriorityQueue.remove()",
            "edu.ncsu.csc316.dsa.priority_queue.HeapAdaptablePriorityQueue",
            "Workshop 9: Priority Queues",
            new MethodSignature("remove", new Class[]{Object.class}, void.class,
                              "public void remove(Entry<K,V> entry)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate adaptable priority queue with specified number of entries, test remove operation"
        ));
        
        ALGORITHMS.put("Workshop 9: Priority Queues", workshop9);
        
        // Workshop 10: Set & Disjoint Set ADTs
        List<AlgorithmConfig> workshop10 = new ArrayList<>();
        
        workshop10.add(new AlgorithmConfig(
            "HashSet.add()",
            "edu.ncsu.csc316.dsa.set.HashSet",
            "Workshop 10: Sets",
            new MethodSignature("add", new Class[]{Object.class}, void.class,
                              "public void add(E value)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average",
            "Test add operation under different collision scenarios: Low, Medium, High"
        ));
        
        workshop10.add(new AlgorithmConfig(
            "HashSet.contains()",
            "edu.ncsu.csc316.dsa.set.HashSet",
            "Workshop 10: Sets",
            new MethodSignature("contains", new Class[]{Object.class}, boolean.class,
                              "public boolean contains(E value)"),
            AnalysisMode.INPUT_SIZE_WITH_COLLISIONS,
            new CollisionRateParameter(1000),
            "O(1) average",
            "Test contains operation under different collision scenarios: Low, Medium, High"
        ));
        
        workshop10.add(new AlgorithmConfig(
            "TreeSet.add()",
            "edu.ncsu.csc316.dsa.set.TreeSet",
            "Workshop 10: Sets",
            new MethodSignature("add", new Class[]{Object.class}, void.class,
                              "public void add(E value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate tree set with specified number of entries, test add operation"
        ));
        
        workshop10.add(new AlgorithmConfig(
            "TreeSet.contains()",
            "edu.ncsu.csc316.dsa.set.TreeSet",
            "Workshop 10: Sets",
            new MethodSignature("contains", new Class[]{Object.class}, boolean.class,
                              "public boolean contains(E value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n)",
            "Pre-populate tree set with specified number of entries, test contains operation"
        ));
        
        workshop10.add(new AlgorithmConfig(
            "UpTreeDisjointSetForest.find()",
            "edu.ncsu.csc316.dsa.disjoint_set.UpTreeDisjointSetForest",
            "Workshop 10: Sets",
            new MethodSignature("find", new Class[]{Object.class}, Object.class,
                              "public Position<E> find(E value)"),
            AnalysisMode.STRUCTURE_SIZE,
            new StructureSizeParameter(),
            "O(log n) with path compression",
            "Pre-populate disjoint set with specified number of elements, test find operation"
        ));
        
        // TEMPORARILY DISABLED: UpTreeDisjointSetForest.union() - classloader issues with Position arguments
        // workshop10.add(new AlgorithmConfig(
        //     "UpTreeDisjointSetForest.union()",
        //     "edu.ncsu.csc316.dsa.disjoint_set.UpTreeDisjointSetForest",
        //     "Workshop 10: Sets",
        //     new MethodSignature("union", new Class[]{Object.class, Object.class}, void.class,
        //                       "public void union(Position<E> s, Position<E> t)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(log n) with union-by-rank",
        //     "Pre-populate disjoint set with specified number of elements, test union operation"
        // ));
        
        ALGORITHMS.put("Workshop 10: Sets", workshop10);
        
        // TEMPORARILY DISABLED: Workshop 11: Graph ADT - complex classloader issues with Vertex/Edge types
        // When enabled, will provide comprehensive graph algorithm analysis
        // List<AlgorithmConfig> workshop11 = new ArrayList<>();
        
        // // EdgeListGraph operations - all core graph methods
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.isDirected()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("isDirected", new Class[]{}, boolean.class,
        //                       "public boolean isDirected()"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Generate graph and check if directed"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.numVertices()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("numVertices", new Class[]{}, int.class,
        //                       "public int numVertices()"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Generate graph and count vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.vertices()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("vertices", new Class[]{}, Iterable.class,
        //                       "public Iterable<Vertex<V>> vertices()"),
        //     AnalysisMode.TRADITIONAL_SCALING,
        //     new InputSizeParameter(),
        //     "O(n)",
        //     "Generate graph and iterate over vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.numEdges()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("numEdges", new Class[]{}, int.class,
        //                       "public int numEdges()"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Generate graph and count edges"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.edges()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("edges", new Class[]{}, Iterable.class,
        //                       "public Iterable<Edge<E>> edges()"),
        //     AnalysisMode.TRADITIONAL_SCALING,
        //     new InputSizeParameter(),
        //     "O(m)",
        //     "Generate graph and iterate over edges"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.insertVertex()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertVertex", new Class[]{Object.class}, Object.class,
        //                       "public Vertex<V> insertVertex(V vertexData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting vertices into graphs with different numbers of existing vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.insertEdge()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertEdge", new Class[]{Object.class, Object.class, Object.class}, Object.class,
        //                       "public Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting edges into graphs with different numbers of existing edges"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.outDegree()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("outDegree", new Class[]{Object.class}, int.class,
        //                       "public int outDegree(Vertex<V> vertex)"),
        //     AnalysisMode.TRADITIONAL_SCALING,
        //     new InputSizeParameter(),
        //     "O(m)",
        //     "Generate graph and calculate out-degree of vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "EdgeListGraph.inDegree()",
        //     "edu.ncsu.csc316.dsa.graph.EdgeListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("inDegree", new Class[]{Object.class}, int.class,
        //                       "public int inDegree(Vertex<V> vertex)"),
        //     AnalysisMode.TRADITIONAL_SCALING,
        //     new InputSizeParameter(),
        //     "O(m)",
        //     "Generate graph and calculate in-degree of vertices"
        // ));
        
        // // AdjacencyListGraph operations - optimized for degree queries
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyListGraph.insertVertex()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertVertex", new Class[]{Object.class}, Object.class,
        //                       "public Vertex<V> insertVertex(V vertexData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting vertices into adjacency list graphs"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyListGraph.insertEdge()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertEdge", new Class[]{Object.class, Object.class, Object.class}, Object.class,
        //                       "public Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting edges into adjacency list graphs"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyListGraph.outDegree()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("outDegree", new Class[]{Object.class}, int.class,
        //                       "public int outDegree(Vertex<V> vertex)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test out-degree calculation in adjacency list graphs - should be O(1)"
        // ));
        
        // // AdjacencyMatrixGraph operations - different complexity trade-offs
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyMatrixGraph.insertVertex()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyMatrixGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertVertex", new Class[]{Object.class}, Object.class,
        //                       "public Vertex<V> insertVertex(V vertexData)"),
        //     AnalysisMode.TRADITIONAL_SCALING,
        //     new InputSizeParameter(),
        //     "O(n)",
        //     "Test inserting vertices into adjacency matrix graphs - requires matrix resize"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyMatrixGraph.getEdge()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyMatrixGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("getEdge", new Class[]{Object.class, Object.class}, Object.class,
        //                       "public Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test edge lookups in adjacency matrix graphs - should be O(1)"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyListGraph.insertVertex()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertVertex", new Class[]{Object.class}, Object.class,
        //                       "public Vertex<V> insertVertex(V vertexData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting vertices into graphs with different numbers of existing vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyListGraph.insertEdge()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyListGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertEdge", new Class[]{Object.class, Object.class, Object.class}, Object.class,
        //                       "public Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting edges into graphs with different numbers of existing edges"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyMatrixGraph.insertVertex()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyMatrixGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertVertex", new Class[]{Object.class}, Object.class,
        //                       "public Vertex<V> insertVertex(V vertexData)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(n²) worst case (resize)",
        //     "Test inserting vertices into graphs with different numbers of existing vertices"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyMatrixGraph.insertEdge()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyMatrixGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("insertEdge", new Class[]{Object.class, Object.class, Object.class}, Object.class,
        //                       "public Edge<E> insertEdge(Vertex<V> vertex1, Vertex<V> vertex2, E edgeData)"),
        //     AnalysisMode.CONSTANT_TIME,
        //     new StructureSizeParameter(),
        //     "O(1)",
        //     "Test inserting edges into graphs with different numbers of existing edges"
        // ));
        
        // workshop11.add(new AlgorithmConfig(
        //     "AdjacencyMapGraph.getEdge()",
        //     "edu.ncsu.csc316.dsa.graph.AdjacencyMapGraph",
        //     "Workshop 11: Graphs",
        //     new MethodSignature("getEdge", new Class[]{Object.class, Object.class}, Object.class,
        //                       "public Edge<E> getEdge(Vertex<V> vertex1, Vertex<V> vertex2)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(1) expected",
        //     "Test edge lookup in graphs with different numbers of vertices and edges"
        // ));
        
        // TEMPORARILY DISABLED: Workshop 11 & 12 (Graphs) - complex classloader issues
        // ALGORITHMS.put("Workshop 11: Graphs", workshop11);
        
        // TEMPORARILY DISABLED: Workshop 12: Graph Algorithms
        // List<AlgorithmConfig> workshop12 = new ArrayList<>();
        
        // workshop12.add(new AlgorithmConfig(
        //     "GraphTraversalUtil.depthFirstSearch()",
        //     "edu.ncsu.csc316.dsa.graph.GraphTraversalUtil",
        //     "Workshop 12: Graph Algorithms",
        //     new MethodSignature("depthFirstSearch", new Class[]{Object.class, Object.class}, Object.class,
        //                       "public static Map<Vertex<V>, Edge<E>> depthFirstSearch(Graph<V,E> graph, Vertex<V> start)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(V + E)",
        //     "Test DFS on graphs with different numbers of vertices and edges"
        // ));
        
        // workshop12.add(new AlgorithmConfig(
        //     "GraphTraversalUtil.breadthFirstSearch()",
        //     "edu.ncsu.csc316.dsa.graph.GraphTraversalUtil",
        //     "Workshop 12: Graph Algorithms",
        //     new MethodSignature("breadthFirstSearch", new Class[]{Object.class, Object.class}, Object.class,
        //                       "public static Map<Vertex<V>, Edge<E>> breadthFirstSearch(Graph<V,E> graph, Vertex<V> start)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(V + E)",
        //     "Test BFS on graphs with different numbers of vertices and edges"
        // ));
        
        // workshop12.add(new AlgorithmConfig(
        //     "ShortestPathUtil.dijkstra()",
        //     "edu.ncsu.csc316.dsa.graph.ShortestPathUtil",
        //     "Workshop 12: Graph Algorithms",
        //     new MethodSignature("dijkstra", new Class[]{Object.class, Object.class}, Object.class,
        //                       "public static Map<Vertex<V>, Integer> dijkstra(Graph<V,E> graph, Vertex<V> start)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O((V + E) log V)",
        //     "Test Dijkstra's algorithm on graphs with different numbers of vertices and edges"
        // ));
        
        // workshop12.add(new AlgorithmConfig(
        //     "MinimumSpanningTreeUtil.kruskal()",
        //     "edu.ncsu.csc316.dsa.graph.MinimumSpanningTreeUtil",
        //     "Workshop 12: Graph Algorithms",
        //     new MethodSignature("kruskal", new Class[]{Object.class}, Object.class,
        //                       "public static PositionalList<Edge<E>> kruskal(Graph<V,E> g)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O(E log E)",
        //     "Test Kruskal's algorithm on graphs with different numbers of vertices and edges"
        // ));
        
        // workshop12.add(new AlgorithmConfig(
        //     "MinimumSpanningTreeUtil.primJarnik()",
        //     "edu.ncsu.csc316.dsa.graph.MinimumSpanningTreeUtil",
        //     "Workshop 12: Graph Algorithms",
        //     new MethodSignature("primJarnik", new Class[]{Object.class}, Object.class,
        //                       "public static PositionalList<Edge<E>> primJarnik(Graph<V,E> g)"),
        //     AnalysisMode.STRUCTURE_SIZE,
        //     new StructureSizeParameter(),
        //     "O((V + E) log V)",
        //     "Test Prim-Jarnik algorithm on graphs with different numbers of vertices and edges"
        // ));
        
        // ALGORITHMS.put("Workshop 12: Graph Algorithms", workshop12);
    }
    
    public static String[] getCategories() {
        return ALGORITHMS.keySet().stream()
            .sorted((a, b) -> {
                // Extract workshop numbers for proper numeric sorting
                int numA = extractWorkshopNumber(a);
                int numB = extractWorkshopNumber(b);
                return Integer.compare(numA, numB);
            })
            .toArray(String[]::new);
    }
    
    private static int extractWorkshopNumber(String category) {
        // Extract number from "Workshop X: ..." format
        if (category.startsWith("Workshop ")) {
            String[] parts = category.split(" ");
            if (parts.length >= 2) {
                try {
                    return Integer.parseInt(parts[1].replace(":", ""));
                } catch (NumberFormatException e) {
                    return 999; // Put non-numeric workshops at end
                }
            }
        }
        return 999; // Put non-workshop categories at end
    }
    
    public static List<AlgorithmConfig> getAlgorithmsForCategory(String category) {
        return ALGORITHMS.getOrDefault(category, new ArrayList<>());
    }
    
    public static List<AlgorithmConfig> getAllAlgorithms() {
        List<AlgorithmConfig> all = new ArrayList<>();
        ALGORITHMS.values().forEach(all::addAll);
        return all;
    }
}