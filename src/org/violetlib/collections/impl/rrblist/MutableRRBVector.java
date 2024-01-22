/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import java.util.function.Function;

import org.violetlib.types.UndefinedValueError;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.rrblist.ArraySupport.*;
import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  This class supports mutating operations used by iterators and builders.
*/

public class MutableRRBVector<V>
  extends FocusableRRBVector<V>
{
    /**
      Create an empty vector.
    */

    public static <V> @NotNull MutableRRBVector<V> create()
    {
        return new MutableRRBVector<>();
    }

    /**
      Create a mutable vector with data with the specified basic vector.
    */

    public static <V> @NotNull MutableRRBVector<V> create(@NotNull RRBVector<V> source)
    {
        return new MutableRRBVector<>(source);
    }

    /**
      Initialize an empty vector.
    */

    protected MutableRRBVector()
    {
    }

    /**
      Initialize a mutable vector with data with the specified vector.
    */

    protected MutableRRBVector(@NotNull RRBVector<V> source)
    {
        super(source);
    }

    /**
      Initialize a vector sharing data with the specified source. The vector is only partially initialized.
    */

    protected MutableRRBVector(boolean isTransient, @NotNull RRBVector<V> source)
    {
        super(isTransient, source);
    }

    /**
      Initialize a vector sharing data with the specified source. The vector is only partially initialized.
    */

    protected MutableRRBVector(boolean isTransient, @NotNull FocusableRRBVector<V> source)
    {
        super(isTransient, source);
    }

    /**
      Initialize a vector with the specified tree.
      @param length The number of list elements.
      @param root The root node of the tree.
      @param depth The depth of the tree.
    */

    protected MutableRRBVector(int length, Object @NotNull [] root, int depth, boolean isTransient)
    {
        super(length, root, depth, isTransient);
    }

    public final void replace(int index, @NotNull V value)
    {
        if (index < focusStart || focusEnd <= index || ((index - focusStart) & ~MASK) != (focus & ~MASK)) {
            if (index < 0 || endIndex <= index) {
                throw new IndexOutOfBoundsException();
            }
            normalizeAndFocusOn(index);
        }
        makeTransientIfNeeded();
        Object[] d1 = copyOf(display1);
        d1[(index - focusStart) & MASK] = value;
        display1 = d1;
    }

    public final void append(@NotNull V value)
    {
        focusOnLastBlock(endIndex);
        int elemIndexInBlock = (endIndex - focusStart) & MASK;
        if /* if next element will go in current block position */ (elemIndexInBlock != 0 || endIndex == 0) {
            appendOnCurrentBlock(value, elemIndexInBlock);
        } else /* next element will go in a new block position */ {
            appendBackNewBlock(value, endIndex);
        }
        endIndex++;
    }

    public final void prepend(@NotNull V value)
    {
        focusOnFirstBlock();
        if (display1.length < WIDTH) {
            prependOnCurrentBlock(value, display1);
        } else {
            prependFrontNewBlock(value);
        }
        endIndex++;
    }

    public final void appendAll(@NotNull RRBVector<? extends V> other)
    {
        if (other.endIndex > 0) {
            if (endIndex > WIDTH2 && other.endIndex <= WIDTH && other.depth == 1) {
                /* appending a small number of elements to a large vector */
                appendArray(other.root);
            } else {
                appendVector(other);
            }
        }
    }

    public final void appendArray(Object @NotNull [] a)
    {
        int _endIndex = endIndex;
        int newEndIndex = endIndex + a.length;
        focusOnLastBlock(_endIndex);
        makeTransientIfNeeded();

        int i = 0;
        while (_endIndex < newEndIndex) {
            int elemIndexInBlock = (_endIndex - focusStart) & MASK;
            if /* next element will go in current block position */ (elemIndexInBlock != 0 || _endIndex == 0) {
                int batchSize = Math.min(WIDTH - elemIndexInBlock, a.length - i);
                Object[] d1 = new Object[elemIndexInBlock + batchSize];
                System.arraycopy(display1, 0, d1, 0, elemIndexInBlock);
                System.arraycopy(a, i, d1, elemIndexInBlock, batchSize);
                display1 = d1;
                _endIndex += batchSize;
                focusEnd = _endIndex;
                i += batchSize;
            } else /* next element will go in a new block position */ {
                appendBackNewBlock((V) a[i], _endIndex);
                _endIndex += 1;
                i += 1;
            }
        }
        assert _endIndex == newEndIndex;
        endIndex = _endIndex;
    }

    public final void appendVector(@NotNull RRBVector<? extends V> other)
    {
        if (other.endIndex == 0) {
            return;
        }

        int thisDepth = this.depth;
        int thatDepth = other.depth;
        if (this.isTransient) {
            this.normalize(thisDepth);
            this.isTransient = false;
        }

        MutableRRBVector<? extends V> that = MutableRRBVector.create(other);

        focusOn(endIndex > 0 ? endIndex - 1 : 0);
        int maxDepth = Math.max(thisDepth, thatDepth);
        switch (maxDepth) {
            case 1: {
                int newSize = endIndex + that.display1.length;
                Object[] concat = rebalancedLeaves(display1, that.display1, true);
                initFromRoot(concat, newSize <= WIDTH ? 1 : 2);
                break;
            }
            case 2: {
                Object[] d1 = that.display1;
                Object[] d2 = that.display2;
                if (((that.focus | that.focusRelax) & ANTI_MASK) != 0) {
                    if (d2 != null) {
                        d1 = (Object[]) d2[0];
                    }
                }
                Object[] concat = rebalancedLeaves(this.display1, d1, false);
                concat = rebalanced(this.display2, concat, that.display2, 2);
                if (concat.length == 2) {
                    initFromRoot((Object[]) concat[0], 2);
                } else {
                    initFromRoot(withComputedSizes(concat, 3), 3);
                }
                break;
            }
            case 3: {
                Object[] d1 = that.display1;
                Object[] d2 = that.display2;
                Object[] d3 = that.display3;
                if (((that.focus | that.focusRelax) & -WIDTH) != 0) {
                    if (d3 != null) {
                        d2 = (Object[]) d3[0];
                    }
                    if (d2 != null) {
                        d1 = (Object[]) d2[0];
                    }
                }
                Object[] concat = rebalancedLeaves(this.display1, d1, false);
                concat = rebalanced(this.display2, concat, d2, 2);
                concat = rebalanced(this.display3, concat, that.display3, 3);
                if (concat.length == 2) {
                    initFromRoot((Object[]) concat[0], 3);
                } else {
                    initFromRoot(withComputedSizes(concat, 4), 4);
                }
                break;
            }
            case 4: {
                Object[] d1 = that.display1;
                Object[] d2 = that.display2;
                Object[] d3 = that.display3;
                Object[] d4 = that.display4;
                if (((that.focus | that.focusRelax) & ANTI_MASK) != 0) {
                    if (d4 != null) {
                        d3 = (Object[]) d4[0];
                    }
                    if (d3 != null) {
                        d2 = (Object[]) d3[0];
                    }
                    if (d2 != null) {
                        d1 = (Object[]) d2[0];
                    }
                }
                Object[] concat = rebalancedLeaves(this.display1, d1, false);
                concat = rebalanced(this.display2, concat, d2, 2);
                concat = rebalanced(this.display3, concat, d3, 3);
                concat = rebalanced(this.display4, concat, that.display4, 4);
                if (concat.length == 2) {
                    initFromRoot((Object[]) concat[0], 4);
                } else {
                    initFromRoot(withComputedSizes(concat, 5), 5);
                }
                break;
            }
            case 5: {
                Object[] d1 = that.display1;
                Object[] d2 = that.display2;
                Object[] d3 = that.display3;
                Object[] d4 = that.display4;
                Object[] d5 = that.display5;
                if (((that.focus | that.focusRelax) & ANTI_MASK) != 0) {
                    if (d5 != null) {
                        d4 = (Object[]) d5[0];
                    }
                    if (d4 != null) {
                        d3 = (Object[]) d4[0];
                    }
                    if (d3 != null) {
                        d2 = (Object[]) d3[0];
                    }
                    if (d2 != null) {
                        d1 = (Object[]) d2[0];
                    }
                }
                Object[] concat = rebalancedLeaves(this.display1, d1, false);
                concat = rebalanced(this.display2, concat, d2, 2);
                concat = rebalanced(this.display3, concat, d3, 3);
                concat = rebalanced(this.display4, concat, d4, 4);
                concat = rebalanced(this.display5, concat, that.display5, 5);
                if (concat.length == 2) {
                    initFromRoot((Object[]) concat[0], 5);
                } else {
                    initFromRoot(withComputedSizes(concat, 6), 6);
                }
                break;
            }
            case 6: {
                Object[] d1 = that.display1;
                Object[] d2 = that.display2;
                Object[] d3 = that.display3;
                Object[] d4 = that.display4;
                Object[] d5 = that.display5;
                Object[] createResult = that.display6;
                if ((that.focus & ANTI_MASK) != 0) {
                    if (createResult != null) {
                        d5 = (Object[]) createResult[0];
                    }
                    if (d5 != null) {
                        d4 = (Object[]) d5[0];
                    }
                    if (d4 != null) {
                        d3 = (Object[]) d4[0];
                    }
                    if (d3 != null) {
                        d2 = (Object[]) d3[0];
                    }
                    if (d2 != null) {
                        d1 = (Object[]) d2[0];
                    }
                }
                Object[] concat = rebalancedLeaves(this.display1, d1, false);
                concat = rebalanced(this.display2, concat, d2, 2);
                concat = rebalanced(this.display3, concat, d3, 3);
                concat = rebalanced(this.display4, concat, d4, 4);
                concat = rebalanced(this.display5, concat, d5, 5);
                concat = rebalanced(this.display6, concat, that.display6, 6);
                if (concat.length == 2) {
                    initFromRoot((Object[]) concat[0], 6);
                } else {
                    //initFromRoot(withComputedSizes(concat, 7), 7);
                    throw new UnsupportedOperationException("Size after insertion exceeds maximum size");
                }
                break;
            }
            default:
                throw new AssertionError("Unexpected maxDepth: " + depth);
        }
        endIndex = endIndex + other.endIndex;
    }

    private Object @NotNull [] rebalanced(Object @Nullable [] displayLeft,
                                          Object @Nullable [] concat,
                                          Object @Nullable [] displayRight,
                                          int currentDepth)
    {
        int leftLength = displayLeft == null ? 0 : displayLeft.length - 1;
        int concatLength = concat == null ? 0 : concat.length - 1;
        int rightLength = displayRight == null ? 0 : displayRight.length - 1;
        int branching = computeBranching(displayLeft, concat, displayRight, currentDepth);
        Object[] top = new Object[(branching >> 10) + ((branching & 1023) == 0 ? 1 : 2)];
        Object[] mid = new Object[(branching >> 10) == 0 ? ((branching + 31) >> 5) + 1 : WIDTH + 1];
        Object[] bot = null;
        int iTop = 0;
        int iMid = 0;
        int iBot = 0;
        int i = 0;
        int j = 0;
        int d = 0;
        Object[] currentDisplay = null;
        int displayEnd = 0;
        do {
            switch (d) {
                case 0:
                    if (displayLeft != null) {
                        currentDisplay = displayLeft;
                        displayEnd = concat == null ? leftLength : leftLength - 1;
                    }
                    break;
                case 1:
                    if (concat == null) {
                        displayEnd = 0;
                    } else {
                        currentDisplay = concat;
                        displayEnd = concatLength;
                    }
                    i = 0;
                    break;
                case 2:
                    if (displayRight != null) {
                        currentDisplay = displayRight;
                        displayEnd = rightLength;
                        i = concat == null ? 0 : 1;
                    }
            }

            while (i < displayEnd) {
                Object[] displayValue = (Object[]) currentDisplay[i];
                int displayValueEnd = currentDepth == 2 ? displayValue.length : displayValue.length - 1;
                if /* the current block in displayValue can be used directly (no copies) */ ((iBot | j) == 0 && displayValueEnd == WIDTH) {
                    if (currentDepth != 2 && bot != null) {
                        withComputedSizes(bot, currentDepth - 1);
                        bot = null;
                    }
                    mid[iMid] = displayValue;
                    i += 1;
                    iMid += 1;
                } else {
                    int numElementsToCopy = Math.min(displayValueEnd - j, WIDTH - iBot);
                    if (iBot == 0) {
                        if (currentDepth != 2 && bot != null)
                            withComputedSizes(bot, currentDepth - 1);
                        bot = new Object[Math.min(branching - (iTop << 10) - (iMid << 5), WIDTH) + (currentDepth == 2 ? 0 : 1)];
                        mid[iMid] = bot;
                    }

                    System.arraycopy(displayValue, j, bot, iBot, numElementsToCopy);
                    j += numElementsToCopy;
                    iBot += numElementsToCopy;
                    if (j == displayValueEnd) {
                        i += 1;
                        j = 0;
                    }

                    if (iBot == WIDTH) {
                        iMid += 1;
                        iBot = 0;
                        if (currentDepth != 2 && bot != null) {
                            withComputedSizes(bot, currentDepth - 1);
                        }
                    }
                }
                if (iMid == WIDTH) {
                    top[iTop] = currentDepth == 1 ? withComputedSizes1(mid) : withComputedSizes(mid, currentDepth);
                    iTop += 1;
                    iMid = 0;
                    int remainingBranches = branching - ((iTop << 10) | (iMid << 5) | iBot);
                    if (remainingBranches > 0) {
                        mid = new Object[(remainingBranches >> 10) == 0 ? (remainingBranches + 63) >> 5 : WIDTH+1];
                    } else {
                        mid = null;
                    }
                }

            }
            d += 1;
        } while (d < 3);

        if (currentDepth != 2 && bot != null) {
            withComputedSizes(bot, currentDepth - 1);
        }

        if (mid != null) {
            top[iTop] = currentDepth == 1 ? withComputedSizes1(mid) : withComputedSizes(mid, currentDepth);
        }
        return top;
    }

    private int computeBranching(Object @Nullable [] displayLeft,
                                 Object @Nullable [] concat,
                                 Object @Nullable [] displayRight,
                                 int currentDepth)
    {
        int leftLength = displayLeft == null ? 0 : displayLeft.length - 1;
        int concatLength = concat == null ? 0 : concat.length - 1;
        int rightLength = displayRight == null ? 0 : displayRight.length - 1;
        int branching = 0;
        if (currentDepth == 1) {
            branching = leftLength + concatLength + rightLength;
            if (leftLength != 0) {
                branching -= 1;
            }
            if (rightLength != 0) {
                branching -= 1;
            }
        } else {
            int i = 0;
            while (i < leftLength - 1) {
                branching += ((Object[]) displayLeft[i]).length;
                i += 1;
            }
            i = 0;
            while (i < concatLength) {
                branching += ((Object[]) concat[i]).length;
                i += 1;
            }
            i = 1;
            while (i < rightLength) {
                branching += ((Object[]) displayRight[i]).length;
                i += 1;
            }
            if (currentDepth != 2) {
                branching -= leftLength + concatLength + rightLength;
                if (leftLength != 0) {
                    branching += 1;
                }
                if (rightLength != 0) {
                    branching += 1;
                }
            }

        }
        return branching;
    }

    private Object @NotNull [] rebalancedLeaves(Object @NotNull [] displayLeft,
                                                Object @NotNull [] displayRight,
                                                boolean isTop)
    {
        int leftLength = displayLeft.length;
        int rightLength = displayRight.length;
        if (leftLength == WIDTH) {
            Object[] top = new Object[3];
            top[0] = displayLeft;
            top[1] = displayRight;
            return top;
        } else if (leftLength + rightLength <= WIDTH) {
            Object[] mergedDisplay = new Object[leftLength + rightLength];
            System.arraycopy(displayLeft, 0, mergedDisplay, 0, leftLength);
            System.arraycopy(displayRight, 0, mergedDisplay, leftLength, rightLength);
            if (isTop)
                return mergedDisplay;
            else {
                Object[] top = new Object[2];
                top[0] = mergedDisplay;
                return top;
            }
        } else {
            Object[] top = new Object[3];
            Object[] arr0 = new Object[WIDTH];
            Object[] arr1 = new Object[leftLength + rightLength - WIDTH];
            top[0] = arr0;
            top[1] = arr1;
            System.arraycopy(displayLeft, 0, arr0, 0, leftLength);
            System.arraycopy(displayRight, 0, arr0, leftLength, WIDTH - leftLength);
            System.arraycopy(displayRight, WIDTH - leftLength, arr1, 0, rightLength - WIDTH + leftLength);
            return top;
        }
    }

    private void appendOnCurrentBlock(V elem, int elemIndexInBlock)
    {
        focusEnd = endIndex;
        Object[] d1 = copyOf(display1, elemIndexInBlock, elemIndexInBlock + 1);
        d1[elemIndexInBlock] = elem;
        display1 = d1;
        makeTransientIfNeeded();
    }

    private void appendBackNewBlock(V elem, int _endIndex)
    {
        int oldDepth = depth;
        int newRelaxedIndex = _endIndex - focusStart + focusRelax;
        int focusJoined = focus | focusRelax;
        int xor = newRelaxedIndex ^ focusJoined;
        boolean _transient = isTransient;
        setupNewBlockInNextBranch(xor, _transient);
        if /* setupNewBlockInNextBranch(...) increased the depth of the tree */ (oldDepth == depth) {
            int level = (xor < WIDTH2) ? 2 : (xor < WIDTH3) ? 3 : (xor < WIDTH4) ? 4 : (xor < WIDTH5) ? 5 : 6;
            if (level < oldDepth) {
                int _focusDepth = focusDepth;
                Object[] display = getDisplay(level + 1);
                do {
                    assert display != null;
                    int displayLen = display.length - 1;
                    int[] oldSizes = (int[]) display[displayLen];
                    int[] newSizes =
                      (level >= _focusDepth && oldSizes != null) ?
                        makeTransientSizes(oldSizes, displayLen - 1)
                        : null;

                    Object[] newDisplay = new Object[display.length];
                    System.arraycopy(display, 0, newDisplay, 0, displayLen - 1);
                    if (level >= _focusDepth) {
                        newDisplay[displayLen] = newSizes;
                    }
                    switch (level) {
                        case 2:
                            display3 = newDisplay;
                            display = display4;
                            break;
                        case 3:
                            display4 = newDisplay;
                            display = display5;
                            break;
                        case 4:
                            display5 = newDisplay;
                            display = display6;
                            break;
                        case 5:
                            display6 = newDisplay;
                    }
                    level += 1;
                } while (level < oldDepth);
            }
        }

        if (oldDepth == focusDepth) {
            initFocus(_endIndex, 0, _endIndex + 1, depth, 0);
        } else {
            initFocus(0, _endIndex, _endIndex + 1, 1, newRelaxedIndex & ANTI_MASK);
        }

        display1[0] = elem;
        isTransient = true;
    }

    private void prependOnCurrentBlock(@NotNull V value, Object @NotNull [] a)
    {
        focusEnd = a.length + 1;
        display1 = copyPrepend1(value, a);
        makeTransientIfNeeded();
    }

    private void prependFrontNewBlock(@NotNull V value)
    {
        int oldDepth = depth;
        boolean _transient = isTransient;
        final int insertionLevel = getPrependInsertionLevel();

        // create new node at this depth and all singleton nodes under it on left most branch
        setupNewBlockInInitBranch(insertionLevel, _transient);

        // update sizes of nodes above the insertion depth
        if /* setupNewBlockInNextBranch(...) increased the depth of the tree */ (oldDepth == depth) {
            int level = insertionLevel;
            if (level < oldDepth) {
                int _focusDepth = focusDepth;
                Object[] display = getDisplay(level + 1);
                do {
                    assert display != null;
                    int displayLen = display.length - 1;
                    int[] newSizes;
                    if (level >= _focusDepth) {
                        newSizes = makeTransientSizes((int[]) display[displayLen], 0); // ***** I changed 1 -> 0
                    } else {
                        assert displayLen == 1;
                        newSizes = new int[displayLen];
                    }
                    Object[] newDisplay = new Object[display.length];
                    System.arraycopy(display, 1, newDisplay, 1, displayLen - 1);
                    newDisplay[displayLen] = newSizes;
                    switch (level) {
                        case 2:
                            display3 = newDisplay;
                            display = display4;
                            break;
                        case 3:
                            display4 = newDisplay;
                            display = display5;
                            break;
                        case 4:
                            display5 = newDisplay;
                            display = display6;
                            break;
                        case 5:
                            display6 = newDisplay;
                    }
                    level += 1;
                } while (level < oldDepth);
            }
        }

        initFocus(0, 0, 1, 1, 0);

        display1[0] = value;
        isTransient = true;
    }

    private int getPrependInsertionLevel()
    {
        // Starting the search for the insertion level at the focus depth does not always work as intended.
        // It fails when the focused subtree is "skinny".

//        int currentLevel = focusDepth;
//        if (currentLevel == 1) {
//            currentLevel += 1;
//        }

        int level = 2;
        Object[] d = getDisplay(level);
        try {
            while (d != null && d.length == 33) {
                level += 1;
                d = getDisplay(level);
            }
        } catch (UnsupportedOperationException ignore) {
        }

        return level;
    }

    private void setupNewBlockInNextBranch(int xor, boolean isTransient)
    {
        if (xor < WIDTH2) {
            if (depth == 1) {
                depth = 2;
                Object[] newRoot = new Object[3][];
                newRoot[0] = display1;
                display2 = newRoot;
            } else {
                Object[] newRoot = copyAndIncRightRoot(display2, isTransient, 1);
                if (isTransient) {
                    int oldTransientBranch = newRoot.length - 3;
                    newRoot[oldTransientBranch] = display1;
                    withRecomputeSizes(newRoot, 2, oldTransientBranch);
                }
                display2 = newRoot;
            }
            display1 = new Object[1];
        } else if (xor < WIDTH3) {
            if (isTransient) {
                normalize(2);
            }
            if (depth == 2) {
                depth = 3;
                display3 = makeNewRoot0(display2);
            } else {
                Object[] newRoot = copyAndIncRightRoot(display3, isTransient, 2);
                if (isTransient) {
                    int oldTransientBranch = newRoot.length - 3;
                    newRoot[oldTransientBranch] = display2;
                    withRecomputeSizes(newRoot, 3, oldTransientBranch);
                }
                display3 = newRoot;
            }
            display1 = new Object[1];
            display2 = EMPTY2;
        } else if (xor < WIDTH4) {
            if (isTransient) {
                normalize(3);
            }
            if (depth == 3) {
                depth = 4;
                display4 = makeNewRoot0(display3);
            } else {
                Object[] newRoot = copyAndIncRightRoot(display4, isTransient, 3);
                if (isTransient) {
                    int transientBranch = newRoot.length - 3;
                    newRoot[transientBranch] = display3;
                    withRecomputeSizes(newRoot, 4, transientBranch);
                }
                display4 = newRoot;
            }
            display1 = new Object[1];
            display2 = EMPTY2;
            display3 = EMPTY2;
        } else if (xor < WIDTH5) {
            if (isTransient) {
                normalize(4);
            }
            if (depth == 4) {
                depth = 5;
                display5 = makeNewRoot0(display4);
            } else {
                Object[] newRoot = copyAndIncRightRoot(display5, isTransient, 4);
                if (isTransient) {
                    int transientBranch = newRoot.length - 3;
                    newRoot[transientBranch] = display4;
                    withRecomputeSizes(newRoot, 5, transientBranch);
                }
                display5 = newRoot;
            }

            display1 = new Object[1];
            display2 = EMPTY2;
            display3 = EMPTY2;
            display4 = EMPTY2;
        } else if (xor < WIDTH6) {
            if (isTransient) {
                normalize(5);
            }
            if (depth == 5) {
                depth = 6;
                display6 = makeNewRoot0(display5);
            } else {
                Object[] newRoot = copyAndIncRightRoot(display6, isTransient, 5);
                if (isTransient) {
                    int transientBranch = newRoot.length - 3;
                    newRoot[transientBranch] = display5;
                    withRecomputeSizes(newRoot, 6, transientBranch);
                }
                display6 = newRoot;
            }
            display1 = new Object[1];
            display2 = EMPTY2;
            display3 = EMPTY2;
            display4 = EMPTY2;
            display5 = EMPTY2;
        } else
            throw new AssertionError("Unexpected xor");
    }

    private void setupNewBlockInInitBranch(int insertionLevel, boolean isTransient)
    {
        switch (insertionLevel) {
            case 2:
                if (depth == 1) {
                    depth = 2;
                    int[] sizes = new int[2];
                    // sizes[0] = 0;
                    sizes[1] = display1.length;
                    Object[] newRoot = new Object[3];
                    newRoot[1] = display1;
                    newRoot[2] = sizes;
                    display2 = newRoot;
                } else {
                    Object[] newRoot = copyAndIncLeftRoot(display2, isTransient, 1);
                    if (isTransient) {
                        withRecomputeSizes(newRoot, 2, 1);
                        newRoot[1] = display1;
                    }
                    display2 = newRoot;
                }
                display1 = new Object[1];
                break;
            case 3:
                if (isTransient) {
                    normalize(2);
                }
                if (depth == 2) {
                    depth = 3;
                    display3 = makeNewRoot1(display2, 2);
                } else {
                    Object[] newRoot = copyAndIncLeftRoot(display3, isTransient, 2);
                    if (isTransient) {
                        withRecomputeSizes(newRoot, 3, 1);
                        newRoot[1] = display2;
                    }
                    display3 = newRoot;
                }
                display2 = EMPTY2;
                display1 = new Object[1];
                break;
            case 4:
                if (isTransient) {
                    normalize(3);
                }
                if (depth == 3) {
                    depth = 4;
                    display4 = makeNewRoot1(display3, 3);
                } else {
                    Object[] newRoot = copyAndIncLeftRoot(display4, isTransient, 3);
                    if (isTransient) {
                        withRecomputeSizes(newRoot, 4, 1);
                        newRoot[1] = display3;
                    }
                    display4 = newRoot;
                }
                display3 = EMPTY2;
                display2 = EMPTY2;
                display1 = new Object[1];
                break;
            case 5:
                if (isTransient) {
                    normalize(4);
                }
                if (depth == 4) {
                    depth = 5;
                    display5 = makeNewRoot1(display4, 4);
                } else {
                    Object[] newRoot = copyAndIncLeftRoot(display5, isTransient, 4);
                    if (isTransient) {
                        withRecomputeSizes(newRoot, 5, 1);
                        newRoot[1] = display4;
                    }
                    display5 = newRoot;
                }
                display4 = EMPTY2;
                display3 = EMPTY2;
                display2 = EMPTY2;
                display1 = new Object[1];
                break;
            case 6:
                if (isTransient) {
                    normalize(5);
                }
                if (depth == 5) {
                    depth = 6;
                    display6 = makeNewRoot1(display5, 5);
                } else {
                    Object[] newRoot = copyAndIncLeftRoot(display6, isTransient, 5);
                    if (isTransient) {
                        withRecomputeSizes(newRoot, 4, 1);
                        newRoot[1] = display5;
                    }
                    display6 = newRoot;
                }
                display5 = EMPTY2;
                display4 = EMPTY2;
                display3 = EMPTY2;
                display2 = EMPTY2;
                display1 = new Object[1];
                break;
            default:
                throw new AssertionError("Unexpected insertion depth: " + insertionLevel);
        }
    }

    private Object @NotNull [] makeNewRoot0(Object @NotNull [] node)
    {
        Object[] newRoot = new Object[3];
        newRoot[0] = node;
        int dLen = node.length;
        int[] dSizes = (int[]) node[dLen - 1];
        if (dSizes != null) {
            int[] newRootSizes = new int[2];
            int dSize = dSizes[dLen - 2];
            newRootSizes[0] = dSize;
            newRootSizes[1] = dSize;
            newRoot[2] = newRootSizes;
        }
        return newRoot;
    }

    private Object @NotNull [] makeNewRoot1(Object @NotNull [] node, int level)
    {
        int dSize = treeSize(node, level);
        int[] newRootSizes = new int[2];
        // newRootSizes[0] = 0;
        newRootSizes[1] = dSize;
        Object[] newRoot = new Object[3];
        newRoot[1] = node;
        newRoot[2] = newRootSizes;
        return newRoot;
    }

    private <T> T @NotNull [] copyAndIncLeftRoot(T @NotNull [] node, boolean isTransient, int level)
    {
        int len = node.length;
        Object[] newRoot = new Object[len+1];
        System.arraycopy(node, 0, newRoot, 1, len - 1);
        int[] oldSizes = (int[]) node[len - 1];
        int[] newSizes = new int[len];
        if (oldSizes != null) {
            if (isTransient) {
                newSizes[0] = 0;
                newSizes[1] = 0;
                System.arraycopy(oldSizes, 1, newSizes, 2, len - 2);
            } else {
                newSizes[0] = 0;
                System.arraycopy(oldSizes, 0, newSizes, 1, len - 1);
            }
        } else {
            int subTreeSize = getMaximumTreeSize(level);
            int acc = 0;
            int i = 1;
            while (i < len - 1) {
                acc += subTreeSize;
                newSizes[i] = acc;
                i += 1;
            }
            newSizes[0] = 0;
            newSizes[i] = acc + treeSize((Object[]) node[node.length - 2], level);
        }
        newRoot[len] = newSizes;
        return (T[]) newRoot;
    }

    private <T> T @NotNull [] copyAndIncRightRoot(T @NotNull [] node, boolean isTransient, int level)
    {
        int len = node.length;
        Object[] newRoot = copyOf(node, len - 1, len + 1);
        int[] oldSizes = (int[]) node[len - 1];
        if (oldSizes != null) {
            int[] newSizes = new int[len];
            System.arraycopy(oldSizes, 0, newSizes, 0, len - 1);
            if (isTransient) {
                newSizes[len - 1] = 1 << (5 * level);
            }
            newSizes[len - 1] = newSizes[len - 2];
            newRoot[len] = newSizes;
        }
        return (T[]) newRoot;
    }

    /**
      Remove all elements except the first {@code n} elements.
      @param n The number of elements to retain. Must be positive.
    */

    public final void retainPrefix(int n)
    {
        // takeFront

        if (n <= 0) {
            throw new IllegalArgumentException("Invalid or unsupported prefix length");
        }
        if (n >= endIndex) {
            return;
        }

        if (depth == 1) {
            focusOn(0);
            Object[] d1 = new Object[n];
            System.arraycopy(display1, 0, d1, 0, n);
            display1 = d1;
            initFocus(0, 0, n, 1, 0);
            endIndex = n;
            return;
        }

        focusOn(n - 1);
        int d0len = (focus & MASK) + 1;
        if (d0len != WIDTH) {
            Object[] d1 = new Object[d0len];
            System.arraycopy(display1, 0, d1, 0, d0len);
            display1 = d1;
        }

        int cutIndex = focus | focusRelax;
        cleanTopTake(cutIndex);
        focusDepth = Math.min(depth, focusDepth);
        copyDisplays(focusDepth, cutIndex);
        int level = depth;
        int offset = 0;
        while (level > focusDepth) {
            Object[] display = getDisplay(level);
            assert display != null;
            int[] oldSizes = (int[]) display[display.length - 1];
            int newLen = ((focusRelax >> (5 * (level - 1))) & MASK) + 1;
            int[] newSizes = new int[newLen];
            System.arraycopy(oldSizes, 0, newSizes, 0, newLen - 1);
            newSizes[newLen - 1] = n - offset;
            if (newLen > 1) {
                offset += newSizes[newLen - 2];
            }

            Object[] newDisplay = new Object[newLen + 1];
            System.arraycopy(display, 0, newDisplay, 0, newLen);
            newDisplay[newLen - 1] = null;
            newDisplay[newLen] = newSizes;
            setDisplay(level, newDisplay);
            level--;
        }
        stabilizeDisplayPath(depth, cutIndex);
        focusEnd = n;
        endIndex = n;
    }

    /**
      Remove the first {@code n} elements.
      @param n The number of elements to remove. Must be non-negative and not greater than the length.
    */

    public final void removePrefix(int n)
    {
        // dropFront

        if (n < 0 || n > endIndex) {
            throw new IllegalArgumentException("Invalid or unsupported prefix length");
        }

        if (n == endIndex) {
            clear();
            return;
        }

        if (depth == 1) {
            focusOn(0);
            int newLen = display1.length - n;
            Object[] d1 = new Object[newLen];
            System.arraycopy(display1, n, d1, 0, newLen);
            display1 = d1;
            initFocus(0, 0, newLen, 1, 0);
            endIndex = newLen;
            return;
        }

        focusOn(n);
        int cutIndex = focus | focusRelax;
        int d1start = cutIndex & MASK;
        if (d1start != 0) {
            int d1len = display1.length - d1start;
            Object[] d1 = new Object[d1len];
            System.arraycopy(display1, d1start, d1, 0, d1len);
            display1 = d1;
        }

        cleanTopDrop(cutIndex);
        int level = 2;
        while (level <= depth) {
            Object[] display = getDisplay(level);
            assert display != null;
            int splitStart = (cutIndex >> (5 * (level - 1))) & MASK;
            int newLen = display.length - splitStart - 1;
            Object[] newDisplay = new Object[newLen + 1];
            System.arraycopy(display, splitStart + 1, newDisplay, 1, newLen - 1);
            newDisplay[0] = getDisplay(level - 1);
            setDisplay(level, withComputedSizes(newDisplay, level));
            level++;
        }

        // This focus not be optimal, but most of the time it will be
        initFocus(0, 0, display1.length, 1, 0);
        endIndex = endIndex - n;
    }

    private void cleanTopTake(int cutIndex)
    {
        int newDepth;

        switch (depth) {
            case 2:
                if (cutIndex < WIDTH) {
                    display2 = null;
                    newDepth = 1;
                } else
                    newDepth = 2;
                this.depth = newDepth;
                return;
            case 3:
                if (cutIndex < WIDTH2) {
                    display3 = null;
                    if (cutIndex < WIDTH) {
                        display2 = null;
                        newDepth = 1;
                    } else
                        newDepth = 2;
                } else
                    newDepth = 3;
                this.depth = newDepth;
                return;
            case 4:
                if (cutIndex < WIDTH3) {
                    display4 = null;
                    if (cutIndex < WIDTH2) {
                        display3 = null;
                        if (cutIndex < WIDTH) {
                            display2 = null;
                            newDepth = 1;
                        } else
                            newDepth = 2;
                    } else
                        newDepth = 3;
                } else
                    newDepth = 4;
                this.depth = newDepth;
                return;
            case 5:
                if (cutIndex < WIDTH4) {
                    display5 = null;
                    if (cutIndex < WIDTH3) {
                        display4 = null;
                        if (cutIndex < WIDTH2) {
                            display3 = null;
                            if (cutIndex < WIDTH) {
                                display2 = null;
                                newDepth = 1;
                            } else
                                newDepth = 2;
                        } else
                            newDepth = 3;
                    } else
                        newDepth = 4;
                } else
                    newDepth = 5;
                this.depth = newDepth;
                return;
            case 6:
                if (cutIndex < WIDTH5) {
                    display6 = null;
                    if (cutIndex < WIDTH4) {
                        display5 = null;
                        if (cutIndex < WIDTH3) {
                            display4 = null;
                            if (cutIndex < WIDTH2) {
                                display3 = null;
                                if (cutIndex < WIDTH) {
                                    display2 = null;
                                    newDepth = 1;
                                } else
                                    newDepth = 2;
                            } else
                                newDepth = 3;
                        } else
                            newDepth = 4;
                    } else
                        newDepth = 5;
                } else
                    newDepth = 6;
                this.depth = newDepth;
        }
    }

    private void cleanTopDrop(int cutIndex)
    {
        int newDepth;

        switch (depth) {
            case 2:
                if ((cutIndex >> BITS1) == display2.length - 2) {
                    display2 = null;
                    newDepth = 1;
                } else
                    newDepth = 2;
                this.depth = newDepth;
                return;
            case 3:
                if ((cutIndex >> BITS2) == display3.length - 2) {
                    display3 = null;
                    if (((cutIndex >> BITS1) & MASK) == display2.length - 2) {
                        display2 = null;
                        newDepth = 1;
                    } else
                        newDepth = 2;
                } else
                    newDepth = 3;
                this.depth = newDepth;
                return;
            case 4:
                if ((cutIndex >> BITS3) == display4.length - 2) {
                    display4 = null;
                    if (((cutIndex >> BITS2) & MASK) == display3.length - 2) {
                        display3 = null;
                        if (((cutIndex >> BITS1) & MASK) == display2.length - 2) {
                            display2 = null;
                            newDepth = 1;
                        } else
                            newDepth = 2;
                    } else
                        newDepth = 3;
                } else
                    newDepth = 4;
                this.depth = newDepth;
                return;
            case 5:
                if ((cutIndex >> BITS4) == display5.length - 2) {
                    display5 = null;
                    if (((cutIndex >> BITS3) & MASK) == display4.length - 2) {
                        display4 = null;
                        if (((cutIndex >> BITS2) & MASK) == display3.length - 2) {
                            display3 = null;
                            if (((cutIndex >> BITS1) & MASK) == display2.length - 2) {
                                display2 = null;
                                newDepth = 1;
                            } else
                                newDepth = 2;
                        } else
                            newDepth = 3;
                    } else
                        newDepth = 4;
                } else
                    newDepth = 5;
                this.depth = newDepth;
                return;
            case 6:
                if ((cutIndex >> BITS5) == display6.length - 2) {
                    display6 = null;
                    if (((cutIndex >> BITS4) & MASK) == display5.length - 2) {
                        display5 = null;
                        if (((cutIndex >> BITS3) & MASK) == display4.length - 2) {
                            display4 = null;
                            if (((cutIndex >> BITS2) & MASK) == display3.length - 2) {
                                display3 = null;
                                if (((cutIndex >> BITS1) & MASK) == display2.length - 2) {
                                    display2 = null;
                                    newDepth = 1;
                                } else
                                    newDepth = 2;
                            } else
                                newDepth = 3;
                        } else
                            newDepth = 4;
                    } else
                        newDepth = 5;
                } else
                    newDepth = 6;
                this.depth = newDepth;
        }
    }

    public final <R> @NotNull MutableRRBVector<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] root = getRoot();
        Object[] resultRoot = internalMapBlock(mapper, root, depth);
        return new MutableRRBVector<>(endIndex, resultRoot, depth, isTransient);
    }

    private <R> Object @NotNull [] internalMapBlock(@NotNull Function<@NotNull V,@NotNull R> mapper,
                                                    Object @NotNull [] block,
                                                    int depth)
    {
        int blockLength = block.length;
        Object[] resultBlock = new Object[blockLength];
        if (depth == 1) {
            // A leaf node
            for (int offset = 0; offset < blockLength; offset++) {
                V item = (V) block[offset];
                R result = mapper.apply(item);
                if (result == null) {
                    throw UndefinedValueError.create("Mapper must not return null");
                }
                resultBlock[offset] = result;
            }
        } else {
            // A branch node
            int[] sizes = (int[]) block[blockLength - 1];
            for (int offset = 0; offset < blockLength - 1; offset++) {
                Object[] subtree = (Object[]) block[offset];
                resultBlock[offset] = internalMapBlock(mapper, subtree, depth - 1);
            }
            resultBlock[blockLength - 1] = sizes;
        }
        return resultBlock;
    }
}
