/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.ListBuilder;

import org.jetbrains.annotations.*;

/**

*/

public final class PrependOrientedListBuilder<V>
  implements ListBuilder<V>
{
    public static <V> @NotNull ListBuilder<V> create()
    {
        return new PrependOrientedListBuilder<>();
    }

    private @Nullable ForwardListNode<V> firstNode;
    private @Nullable ForwardListNode<V> node;
    private @Nullable V lastItem;
    private int size;

    private PrependOrientedListBuilder()
    {
    }

    @Override
    public void add(@NotNull V e)
    {
        ForwardListNode<V> nextNode = ForwardListNode.create(e, EmptyForwardList.empty());
        if (firstNode == null) {
            firstNode = nextNode;
        } else {
            assert node != null;
            node.installNext(nextNode);
        }
        node = nextNode;
        size = size + 1;
        lastItem = e;
    }

    @Override
    public void reset()
    {
        firstNode = null;
        node = null;
        lastItem = null;
        size = 0;
    }

    @Override
    public boolean isEmpty()
    {
        return firstNode == null;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public @Nullable V lastItem()
    {
        return lastItem;
    }

    @Override
    public @NotNull PrependOrientedList<V> values()
    {
        if (firstNode == null) {
            return EmptyForwardList.empty();
        }

        ForwardListNode<V> currentNode = firstNode;
        int currentSize = size;
        while (currentNode != null) {
            currentNode = currentNode.installSize(currentSize--);
        }
        assert currentSize == 0;
        return firstNode;
    }
}
