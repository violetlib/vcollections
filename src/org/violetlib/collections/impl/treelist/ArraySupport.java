/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Function;

import org.violetlib.types.UndefinedValueError;

import org.jetbrains.annotations.*;
/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class ArraySupport
{
    private ArraySupport()
    {
    }

    static final Object @NotNull [] EMPTY1 = new Object[0];
    static final Object @NotNull [][] EMPTY2 = new Object[0][];
    static final Object @NotNull [][][] EMPTY3 = new Object[0][][];
    static final Object @NotNull [][][][] EMPTY4 = new Object[0][][][];
    static final Object @NotNull [][][][][] EMPTY5 = new Object[0][][][][];
    static final Object @NotNull [][][][][][] EMPTY6 = new Object[0][][][][][];

    public static Object @NotNull [] createArray(int dimension, int length)
    {
        switch (dimension) {
            case 1: return new Object[length];
            case 2: return new Object[length][];
            case 3: return new Object[length][][];
            case 4: return new Object[length][][][];
            case 5: return new Object[length][][][][];
            case 6: return new Object[length][][][][][];
            default: throw new UnsupportedOperationException("Unsupported array dimension: " + dimension);
        }
    }

    public static Object @NotNull [] copyPrepend1(@NotNull Object element, Object @NotNull [] data)
    {
        int len = data.length;
        Object[] result = new Object[len+1];
        System.arraycopy(data, 0, result, 1, len);
        result[0] = element;
        return result;
    }

    public static Object @NotNull [] copyAppend1(Object @NotNull [] data, @NotNull Object element)
    {
        int len = data.length;
        Object[] result = new Object[len+1];
        System.arraycopy(data, 0, result, 0, len);
        result[len] = element;
        return result;
    }

    public static <T> T @NotNull [] copyAppend(T @NotNull [] a, @NotNull T element)
    {
        T[] result = Arrays.copyOf(a, a.length+1);
        result[result.length - 1] = element;
        return result;
    }

    public static <T> T @NotNull [][] copyAppend(T @NotNull [][] a2, T @NotNull [] a1, @NotNull T element)
    {
        return copyAppend(a2, copyAppend(a1, element));
    }

    public static <T> T @NotNull [][][] copyAppend(T @NotNull [][][] a3,
                                                   T @NotNull [][] a2,
                                                   T @NotNull [] a1,
                                                   @NotNull T element)
    {
        return copyAppend(a3, copyAppend(a2, a1, element));
    }

    public static <T> T @NotNull [][][][] copyAppend(T @NotNull [][][][] a4,
                                                     T @NotNull [][][] a3,
                                                     T @NotNull [][] a2,
                                                     T @NotNull [] a1,
                                                     @NotNull T element)
    {
        return copyAppend(a4, copyAppend(a3, a2, a1, element));
    }

    public static <T> T @NotNull [][][][][] copyAppend(T @NotNull [][][][][] a5,
                                                       T @NotNull [][][][] a4,
                                                       T @NotNull [][][] a3,
                                                       T @NotNull [][] a2,
                                                       T @NotNull [] a1,
                                                       @NotNull T element)
    {
        return copyAppend(a5, copyAppend(a4, a3, a2, a1, element));
    }

    public static <T> T @NotNull [] copyPrepend(@NotNull T element, T @NotNull [] a)
    {
        int len = a.length;
        T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), len+1);
        System.arraycopy(a, 0, result, 1, len);
        result[0] = element;
        return result;
    }

    public static <T> T @NotNull [][] copyPrepend(@NotNull T element, T @NotNull [] a1, T @NotNull [][] a2)
    {
        return copyPrepend(copyPrepend(element, a1), a2);
    }

    public static <T> T @NotNull [][][] copyPrepend(@NotNull T element,
                                                    T @NotNull [] a1,
                                                    T @NotNull [][] a2,
                                                    T @NotNull [][][] a3)
    {
        return copyPrepend(copyPrepend(element, a1, a2), a3);
    }

    public static <T> T @NotNull [][][][] copyPrepend(@NotNull T element,
                                                      T @NotNull [] a1,
                                                      T @NotNull [][] a2,
                                                      T @NotNull [][][] a3,
                                                      T @NotNull [][][][] a4)
    {
        return copyPrepend(copyPrepend(element, a1, a2, a3), a4);
    }

    public static <T> T @NotNull [][][][][] copyPrepend(@NotNull T element,
                                                        T @NotNull [] a1,
                                                        T @NotNull [][] a2,
                                                        T @NotNull [][][] a3,
                                                        T @NotNull [][][][] a4,
                                                        T @NotNull [][][][][] a5)
    {
        return copyPrepend(copyPrepend(element, a1, a2, a3, a4), a5);
    }

    public static Object @NotNull [] wrap1(@NotNull Object element)
    {
        Object[] result = new Object[1];
        result[0] = element;
        return result;
    }

    public static Object @NotNull [][] wrap2(Object @NotNull [] a)
    {
        Object[][] result = new Object[1][];
        result[0] = a;
        return result;
    }

    public static Object @NotNull [][][] wrap3(Object @NotNull [][] a)
    {
        Object[][][] result = new Object[1][][];
        result[0] = a;
        return result;
    }

    public static Object @NotNull [][][][] wrap4(Object @NotNull [][][] a)
    {
        Object[][][][] result = new Object[1][][][];
        result[0] = a;
        return result;
    }

    public static Object @NotNull [][][][][] wrap5(Object @NotNull [][][][] a)
    {
        Object[][][][][] result = new Object[1][][][][];
        result[0] = a;
        return result;
    }

    /**
      Perform a map operation on an array of elements.
      @param elements The source elements.
      @param mapper The map function.
      @return a new array containing the results of the map operation.
    */

    public static <V,R> Object @NotNull [] map1(Object @NotNull [] elements,
                                                @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] resultElements = new Object[elements.length];
        int resultCount = 0;
        for (Object element : elements) {
            R replacement = mapper.apply((V) element);
            if (replacement == null) {
                throw UndefinedValueError.create("Mapper must not return null");
            }
            resultElements[resultCount++] = replacement;
        }
        return resultElements;
    }

    public static <V,R> Object @NotNull [][] map2(Object @NotNull [][] tree,
                                                  @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        int childCount = tree.length;
        Object[][] resultTree = new Object[childCount][];
        for (int i = 0; i < childCount; i++) {
            resultTree[i] = map1(tree[i], mapper);
        }
        return resultTree;
    }

    public static <V,R> Object @NotNull [][][] map3(Object @NotNull [][][] tree,
                                                    @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        int childCount = tree.length;
        Object[][][] resultTree = new Object[childCount][][];
        for (int i = 0; i < childCount; i++) {
            resultTree[i] = map2(tree[i], mapper);
        }
        return resultTree;
    }

    public static <V,R> Object @NotNull [][][][] map4(Object @NotNull [][][][] tree,
                                                      @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        int childCount = tree.length;
        Object[][][][] resultTree = new Object[childCount][][][];
        for (int i = 0; i < childCount; i++) {
            resultTree[i] = map3(tree[i], mapper);
        }
        return resultTree;
    }

    public static <V,R> Object @NotNull [][][][][] map5(Object @NotNull [][][][][] tree,
                                                        @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        int childCount = tree.length;
        Object[][][][][] resultTree = new Object[childCount][][][][];
        for (int i = 0; i < childCount; i++) {
            resultTree[i] = map4(tree[i], mapper);
        }
        return resultTree;
    }

    public static <V,R> Object @NotNull [][][][][][] map6(Object @NotNull [][][][][][] tree,
                                                          @NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        int childCount = tree.length;
        Object[][][][][][] resultTree = new Object[childCount][][][][][];
        for (int i = 0; i < childCount; i++) {
            resultTree[i] = map5(tree[i], mapper);
        }
        return resultTree;
    }

    /**
      Conditionally append the values from an iterable source to an array. For this operation to be performed, it must
      be possible to quickly determine the number of values in the source and that number must not exceed a
      specified maximum.
      @param a The array to which the source values would be appended.
      @param values The values to append.
      @param available The maximum number of values to permit the append to be performed.
      @return the result of appending the values to the array, or null if the append could not be performed.
    */

    public static Object @Nullable [] append1IfSpace(Object @NotNull [] a, @NotNull Iterable<?> values, int available)
    {
        int count = IterableSupport.quickCount(values);
        if (count >= 0) {
            if (count == 0) {
                return a;
            }
            if (count <= available) {
                if (count == 1) {
                    Object first = IterableSupport.first(values);
                    if (first == null) {
                        throw new IllegalArgumentException("Values element is null");
                    }
                    return copyAppend(a, first);
                } else {
                    Object[] result = Arrays.copyOf(a, a.length + count);
                    int index = a.length;
                    for (Object o : values) {
                        if (o == null) {
                            throw new IllegalArgumentException("Values contains null");
                        }
                        result[index++] = o;
                    }
                    return result;
                }
            }
        }
        return null;
    }
}
