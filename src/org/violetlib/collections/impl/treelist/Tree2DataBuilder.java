/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.treelist.TreeListConstants.*;

/**

*/

public final class Tree2DataBuilder
{
    public static @NotNull Tree2DataBuilder create()
    {
        return new Tree2DataBuilder();
    }

    private final Object @NotNull [][] top = new Object[WIDTH][];
    private int currentTopIndex = 0;
    private int currentLeafIndex = 0;
    private int size = 0;

    private Tree2DataBuilder()
    {
    }

    public void addElements(Object @NotNull [] elements)
    {
        for (Object element : elements) {
            addElement(element);
        }
    }

    public void addElement(@NotNull Object element)
    {
        Object[] currentLeaf = top[currentTopIndex];
        if (currentLeaf.length == WIDTH) {
            ++currentTopIndex;
            if (currentTopIndex == WIDTH) {
                throw new UnsupportedOperationException("Too many elements");
            }
            currentLeaf = top[currentTopIndex];
            currentLeafIndex = 0;
        }
        currentLeaf[currentLeafIndex++] = element;
        ++size;
    }

    public int size()
    {
        return size;
    }

    public Object @Nullable [][] getTree()
    {
        if (currentTopIndex == 0 && currentLeafIndex == 0) {
            // no elements
            return null;
        }
        int topSize = currentTopIndex + 1;
        if (topSize == WIDTH) {
            return top;
        }
        Object[][] trimmed = new Object[topSize][];
        System.arraycopy(top, 0, trimmed, 0, topSize);
        return trimmed;
    }
}
