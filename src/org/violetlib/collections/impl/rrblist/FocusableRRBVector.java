/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import java.util.Arrays;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.rrblist.ArraySupport.*;
import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  This class supports a relaxed radix balanced tree with a transient state and an optional focused subtree.
  <p>
  The focused subtree contains a designated element (the focused element).
  The focused subtree must not contain any relaxed branch nodes.
  The path from the root to the focused element can be split into two parts: the path from the root to the
  focused subtree and the path from the root of the focused subtree to the focused element.
  <p>
  When there is no focused subtree, the subtree element range is [0,1).
  <p>
  The transient state effectively transfers the focused path from the tree to a separate display.
  <p>
  Although this class and its subclasses are mutable, the individual tree nodes use copy on write. Nodes can be shared
  safely with ordinary vectors.
*/

public class FocusableRRBVector<V>
{
    /**
      create an empty vector.
    */

    public static <V> @NotNull FocusableRRBVector<V> create()
    {
        return new FocusableRRBVector<>();
    }

    /**
      Create a focusable vector with data with the specified basic vector.
    */

    public static <V> @NotNull FocusableRRBVector<V> create(@NotNull RRBVector<V> source)
    {
        return new FocusableRRBVector<>(source);
    }

    static final Object @NotNull [] EMPTY2 = new Object[2][];

    protected boolean isTransient = false;

    /**
      The depth of the tree, which is the number of nodes in any tree path.
      A value of 1 indicates that the tree consists of a single leaf node that may be empty.
      The largest supported depth is 6.
    */

    protected int depth;

    /**
      One greater than the last valid index (i.e., the length).
    */

    protected int endIndex;

    /**
      The index of the designated element in the focused subtree relative to the index of the first element in the
      focused subtree.
    */

    protected int focus;

    /**
      The index of the first element in the focused subtree.
    */

    protected int focusStart;

    /**
      One greater than the index of the last element in the focused subtree.
    */

    protected int focusEnd;

    /**
      The depth of the focused subtree.
    */

    protected int focusDepth;

    /**
      A tree path to the designated element. Used like an index in a non-relaxed tree, but it is not the index of the
      focused element.
    */

    protected int focusRelax;

    /**
      The display. The level numbers match the depth of the tree.
    */

    protected Object[] display1;  // level 1 node (contains elements) or an empty node (for an empty tree)
    protected Object[] display2;  // level 2 node (contains level 1 nodes)
    protected Object[] display3;  // level 3 node (contains level 2 nodes)
    protected Object[] display4;  // level 4 node (contains level 3 nodes)
    protected Object[] display5;  // level 5 node (contains level 4 nodes)
    protected Object[] display6;  // level 6 node (contains level 5 nodes)

    /**
      Initialize an empty vector.
    */

    protected FocusableRRBVector()
    {
        endIndex = 0;
        display1 = new Object[0];
        focusEnd = 1;
        depth = 1;
    }

    /**
      Initialize a focusable vector with data with the specified basic vector.
    */

    protected FocusableRRBVector(@NotNull RRBVector<V> source)
    {
        endIndex = source.endIndex;
        depth = source.depth;
        initFromRoot(source.root, depth);
    }

    /**
      Initialize a vector with data with the specified source. The vector is only partially initialized.
    */

    protected FocusableRRBVector(boolean isTransient, @NotNull RRBVector<V> source)
    {
        this.endIndex = source.endIndex;
        this.isTransient = isTransient;
        this.depth = source.depth;
        initFromRoot(source.root, depth);
    }

    /**
      Initialize a vector sharing data with the specified source. The vector is only partially initialized.
    */

    protected FocusableRRBVector(boolean isTransient, @NotNull FocusableRRBVector<V> source)
    {
        this.isTransient = isTransient;
        initFocus(source.focus, source.focusStart, source.focusEnd, source.focusDepth, source.focusRelax);
        initFrom(source);
    }

    /**
      Initialize a vector with the specified tree.
      @param length The number of list elements.
      @param root The root node of the tree.
      @param depth The depth of the tree.
    */

    protected FocusableRRBVector(int length, Object @NotNull [] root, int depth, boolean isTransient)
    {
        this.depth = depth;
        this.endIndex = length;
        this.isTransient = isTransient;
        initFromRoot(root, depth);
    }

    protected void initFromRoot(Object @NotNull [] root, int depth)
    {
        this.depth = depth;
        switch (depth) {
            case 1: display1 = root; break;
            case 2: display2 = root; break;
            case 3: display3 = root; break;
            case 4: display4 = root; break;
            case 5: display5 = root; break;
            case 6: display6 = root; break;
            default: throw new AssertionError("Unexpected depth: " + depth);
        }
        focusEnd = focusStart;
        focusOn(0);
    }

    protected void initFrom(@NotNull FocusableRRBVector<V> source)
    {
        endIndex = source.endIndex;
        depth = source.depth;
        switch (depth) {
            case 1:
                this.display1 = source.display1;
                return;
            case 2:
                this.display1 = source.display1;
                this.display2 = source.display2;
                return;
            case 3:
                this.display1 = source.display1;
                this.display2 = source.display2;
                this.display3 = source.display3;
                return;
            case 4:
                this.display1 = source.display1;
                this.display2 = source.display2;
                this.display3 = source.display3;
                this.display4 = source.display4;
                return;
            case 5:
                this.display1 = source.display1;
                this.display2 = source.display2;
                this.display3 = source.display3;
                this.display4 = source.display4;
                this.display5 = source.display5;
                return;
            case 6:
                this.display1 = source.display1;
                this.display2 = source.display2;
                this.display3 = source.display3;
                this.display4 = source.display4;
                this.display5 = source.display5;
                this.display6 = source.display6;
                return;
        }
    }

    public void clear()
    {
        endIndex = 0;
        display1 = new Object[0];
        display2 = null;
        display3 = null;
        display4 = null;
        display5 = null;
        display6 = null;
        focusEnd = 1;
        depth = 1;
        isTransient = false;
    }

    public @NotNull RRBVector<V> asBasic()
    {
        if (isTransient) {
            normalize(depth);
            isTransient = false;
        }
        return new RRBVector<>(endIndex, getRoot(), depth);
    }

    protected @NotNull V get(int index)
    {
        if (index >= focusStart && index < focusEnd) {
            return getElemFromInsideFocus(index, focusStart);
        } else {
            return getElemFromOutsideFocus(index);
        }
    }

    protected @NotNull V getElemFromOutsideFocus(int index)
    {
        if (index >= 0 && index < endIndex) {
            if (isTransient) {
                normalize(depth);
                isTransient = false;
            }
            return getElementFromRoot(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    protected @NotNull V getElemFromInsideFocus(int index, int focusStart)
    {
        int indexInFocus = index - focusStart;
        return getElem(indexInFocus, indexInFocus ^ focus);
    }

    private @NotNull V getElem(int index, int xor)
    {
        if (xor < WIDTH1) {
            return getElem0(display1, index);
        }
        if (xor < WIDTH2) {
            return getElem1(display2, index);
        }
        if (xor < WIDTH3) {
            return getElem2(display3, index);
        }
        if (xor < WIDTH4) {
            return getElem3(display4, index);
        }
        if (xor < WIDTH5) {
            return getElem4(display5, index);
        }
        if (xor < WIDTH6) {
            return getElem5(display6, index);
        }
        throw new AssertionError("Invalid xor: " + xor);
    }

    protected @NotNull V getElem0(Object @NotNull [] a0, int index) {
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem1(Object @NotNull [] a1, int index)
    {
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem2(Object @NotNull [] a2, int index)
    {
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem3(Object @NotNull [] a3, int index)
    {
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem4(Object @NotNull [] a4, int index)
    {
        Object[] a3 = (Object[]) a4[(index >> BITS4) & MASK];
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem5(Object @NotNull [] a5, int index)
    {
        Object[] a4 = (Object[]) a5[(index >> BITS5) & MASK];
        Object[] a3 = (Object[]) a4[(index >> BITS4) & MASK];
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElementFromRoot(int index)
    {
        int indexInSubTree = index;
        int currentLevel = depth;
        Object[] node = getRoot();
        int[] sizes = getSizes(currentLevel, node);
        do {
            int sizesIdx = sizes != null ? getIndexInSizes(sizes, indexInSubTree) : 0;
            if (sizesIdx != 0) {
                indexInSubTree -= sizes[sizesIdx - 1];
            }
            node = (Object[]) node[sizesIdx];
            --currentLevel;
            sizes = getSizes(currentLevel, node);
        } while (sizes != null);

        switch (currentLevel) {
            case 1: return getElem0(node, indexInSubTree);
            case 2: return getElem1(node, indexInSubTree);
            case 3: return getElem2(node, indexInSubTree);
            case 4: return getElem3(node, indexInSubTree);
            case 5: return getElem4(node, indexInSubTree);
            case 6: return getElem5(node, indexInSubTree);
        }
        throw new AssertionError("Unexpected depth: " + currentLevel);
    }

    int getIndexInSizes(int @NotNull [] sizes, int indexInSubTree)
    {
        if (indexInSubTree == 0) {
            return 0;
        }
        int is = 0;
        while (sizes[is] <= indexInSubTree) {
            is += 1;
        }
        return is;
    }

    protected void gotoPosFromRoot(int index)
    {
        if (endIndex == 0) {
            return;
        }

        int _startIndex = 0;
        int _endIndex = endIndex;
        int currentLevel = depth;
        int _focusRelax = 0;
        boolean keepGoing = currentLevel > 1;

        if (keepGoing) {
            Object[] display = getDisplay(currentLevel);
            do {
                assert display != null;
                int[] sizes = getSizes(currentLevel, display);
                if (sizes == null) {
                    keepGoing = false;
                } else {
                    int is = getIndexInSizes(sizes, index - _startIndex);
                    display = (Object[]) display[is];
                    switch (currentLevel) {
                        case 2:
                            display1 = display;
                            keepGoing = false;
                            break;
                        case 3:
                            display2 = display;
                            break;
                        case 4:
                            display3 = display;
                            break;
                        case 5:
                            display4 = display;
                            break;
                        case 6:
                            display5 = display;
                            break;
                    }
                    if (is < sizes.length - 1) {
                        _endIndex = _startIndex + sizes[is];
                    }
                    if (is != 0) {
                        _startIndex += sizes[is - 1];
                    }
                    currentLevel -= 1;
                    _focusRelax |= is << (BITS1 * currentLevel);
                }
            } while (keepGoing);
        }
        int indexInFocus = index - _startIndex;
        gotoPos(indexInFocus, 1 << (BITS1 * (currentLevel - 1)));
        initFocus(indexInFocus, _startIndex, _endIndex, currentLevel, _focusRelax);
    }

    protected void gotoPos(int index, int xor)
    {
        if (xor < WIDTH) {
        } else if (xor < WIDTH2) {
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH3) {
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH4) {
            display3 = (Object[]) display4[(index >> BITS3) & MASK];
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH5) {
            display4 = (Object[]) display5[(index >> BITS4) & MASK];
            display3 = (Object[]) display4[(index >> BITS3) & MASK];
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH6) {
            display5 = (Object[]) display6[(index >> BITS5) & MASK];
            display4 = (Object[]) display5[(index >> BITS4) & MASK];
            display3 = (Object[]) display4[(index >> BITS3) & MASK];
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else {
            throw new AssertionError("Unexpected xor: " + xor);
        }
    }

    protected void gotoNextBlockStart(int index, int xor)
    {
        if (xor < WIDTH2) {
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH3) {
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[0];
        } else if (xor < WIDTH4) {
            display3 = (Object[]) display4[(index >> BITS3) & MASK];
            display2 = (Object[]) display3[0];
            display1 = (Object[]) display2[0];
        } else if (xor < WIDTH5) {
            display4 = (Object[]) display5[(index >> BITS4) & MASK];
            display3 = (Object[]) display4[0];
            display2 = (Object[]) display3[0];
            display1 = (Object[]) display2[0];
        } else if (xor < WIDTH6) {
            display5 = (Object[]) display6[(index >> BITS5) & MASK];
            display4 = (Object[]) display5[0];
            display3 = (Object[]) display4[0];
            display2 = (Object[]) display3[0];
            display1 = (Object[]) display2[0];
        } else {
            throw new AssertionError("Unexpected xor: " + xor);
        }
    }

    protected void gotoPrevBlockStart(int index, int xor)
    {
        if (xor < WIDTH2) {
            display1 = (Object[]) display2[(index >> BITS1) & MASK];
        } else if (xor < WIDTH3) {
            display2 = (Object[]) display3[(index >> BITS2) & MASK];
            display1 = (Object[]) display2[WIDTH-1];
        } else if (xor < WIDTH4) {
            display3 = (Object[]) display4[(index >> BITS3) & MASK];
            display2 = (Object[]) display3[WIDTH-1];
            display1 = (Object[]) display2[WIDTH-1];
        } else if (xor < WIDTH5) {
            display4 = (Object[]) display5[(index >> BITS4) & MASK];
            display3 = (Object[]) display4[WIDTH-1];
            display2 = (Object[]) display3[WIDTH-1];
            display1 = (Object[]) display2[WIDTH-1];
        } else if (xor < WIDTH6) {
            display5 = (Object[]) display6[(index >> BITS5) & MASK];
            display4 = (Object[]) display5[WIDTH-1];
            display3 = (Object[]) display4[WIDTH-1];
            display2 = (Object[]) display3[WIDTH-1];
            display1 = (Object[]) display2[WIDTH-1];
        } else {
            throw new AssertionError("Unexpected xor: " + xor);
        }
    }

    protected void initFocus(int focus, int focusStart, int focusEnd, int focusDepth, int focusRelax)
    {
        this.focus = focus;
        this.focusStart = focusStart;
        this.focusEnd = focusEnd;
        this.focusDepth = focusDepth;
        this.focusRelax = focusRelax;
    }

    protected void focusOnFirstBlock()
    {
        if (focusStart != 0 || (focus & ANTI_MASK) != 0) {
            /* the current focused block is not on the left most leaf block of the vector */
            normalizeAndFocusOn(0);
        }
    }

    protected void focusOnLastBlock(int _endIndex)
    {
        if /* vector focus is not focused block of the last element */ (((focusStart + focus) ^ (_endIndex - 1)) >= WIDTH) {
            normalizeAndFocusOn(_endIndex - 1);
        }
    }

    protected void focusOn(int index)
    {
        int _focusStart = focusStart;
        if (_focusStart <= index && index < focusEnd) {
            int indexInFocus = index - _focusStart;
            int xor = indexInFocus ^ focus;
            if (xor >= WIDTH) {
                gotoPos(indexInFocus, xor);
            }
            focus = indexInFocus;
        } else {
            gotoPosFromRoot(index);
        }
    }

    protected void makeTransientIfNeeded()
    {
        if (depth > 1 && !isTransient) {
            copyDisplaysAndNullFocusedBranch(depth, focus | focusRelax);
            isTransient = true;
        }
    }

    protected void normalizeAndFocusOn(int index)
    {
        if (isTransient) {
            normalize(depth);
            isTransient = false;
        }
        focusOn(index);
    }

    protected void normalize(int _depth)
    {
        assert _depth > 1;
        int _focusDepth = focusDepth;
        int stabilizationIndex = focus | focusRelax;
        copyDisplaysAndStabilizeDisplayPath(_focusDepth, stabilizationIndex);

        int currentLevel = _focusDepth;
        if (currentLevel < _depth) {
            Object[] display = getDisplay(currentLevel + 1);
            do {
                assert display != null;
                Object[] newDisplay = Arrays.copyOf(display, display.length);
                int idx = (stabilizationIndex >> (BITS1 * currentLevel)) & MASK;
                switch (currentLevel) {
                    case 1:
                        newDisplay[idx] = display1;
                        display2 = withRecomputeSizes(newDisplay, currentLevel + 1, idx);
                        display = display3;
                        break;
                    case 2:
                        newDisplay[idx] = display2;
                        display3 = withRecomputeSizes(newDisplay, currentLevel + 1, idx);
                        display = display4;
                        break;
                    case 3:
                        newDisplay[idx] = display3;
                        display4 = withRecomputeSizes(newDisplay, currentLevel + 1, idx);
                        display = display5;
                        break;
                    case 4:
                        newDisplay[idx] = display4;
                        display5 = withRecomputeSizes(newDisplay, currentLevel + 1, idx);
                        display = display6;
                        break;
                    case 5:
                        newDisplay[idx] = display5;
                        display6 = withRecomputeSizes(newDisplay, currentLevel + 1, idx);
                }
                currentLevel += 1;
            } while (currentLevel < _depth);
        }
    }

    protected void copyDisplays(int _depth, int _focus)
    {
        if (_depth >= 2) {
            int idx1 = ((_focus >> BITS1) & MASK) + 1;
            display2 = copyOf(display2, idx1, idx1 + 1);
            if (_depth >= 3) {
                int idx2 = ((_focus >> BITS2) & MASK) + 1;
                display3 = copyOf(display3, idx2, idx2 + 1);
                if (_depth >= 4) {
                    int idx3 = ((_focus >> BITS3) & MASK) + 1;
                    display4 = copyOf(display4, idx3, idx3 + 1);
                    if (_depth >= 5) {
                        int idx4 = ((_focus >> BITS4) & MASK) + 1;
                        display5 = copyOf(display5, idx4, idx4 + 1);
                        if (_depth >= 6) {
                            int idx5 = ((_focus >> BITS5) & MASK) + 1;
                            display6 = copyOf(display6, idx5, idx5 + 1);
                        }
                    }
                }
            }
        }
    }

    protected void copyDisplaysAndNullFocusedBranch(int _depth, int _focus) {
        switch (_depth) {
            case 2:
                display2 = copyOfAndNull(display2, (_focus >> BITS1) & MASK);
                return;
            case 3:
                display2 = copyOfAndNull(display2, (_focus >> BITS1) & MASK);
                display3 = copyOfAndNull(display3, (_focus >> BITS2) & MASK);
                return;
            case 4:
                display2 = copyOfAndNull(display2, (_focus >> BITS1) & MASK);
                display3 = copyOfAndNull(display3, (_focus >> BITS2) & MASK);
                display4 = copyOfAndNull(display4, (_focus >> BITS3) & MASK);
                return;
            case 5:
                display2 = copyOfAndNull(display2, (_focus >> BITS1) & MASK);
                display3 = copyOfAndNull(display3, (_focus >> BITS2) & MASK);
                display4 = copyOfAndNull(display4, (_focus >> BITS3) & MASK);
                display5 = copyOfAndNull(display5, (_focus >> BITS4) & MASK);
                return;
            case 6:
                display2 = copyOfAndNull(display2, (_focus >> BITS1) & MASK);
                display3 = copyOfAndNull(display3, (_focus >> BITS2) & MASK);
                display4 = copyOfAndNull(display4, (_focus >> BITS3) & MASK);
                display5 = copyOfAndNull(display5, (_focus >> BITS4) & MASK);
                display6 = copyOfAndNull(display6, (_focus >> BITS5) & MASK);
        }
    }

    protected static <T> T @NotNull [] copyOfAndNull(T @NotNull [] a, int nullIndex) {
        int len = a.length;
        Object[] result = new Object[len];
        System.arraycopy(a, 0, result, 0, len - 1);
        result[nullIndex] = null;
        int[] sizes = getSizes(a);
        if (sizes != null) {
            installSizes(result, makeTransientSizes(sizes, nullIndex));
        }
        return (T[]) result;
    }

    protected void copyDisplaysAndStabilizeDisplayPath(int _depth, int _focus) {
        switch (_depth) {
            case 1:
                return;
            case 2:
                Object[] d2 = copyOf(display2);
                d2[(_focus >> BITS1) & MASK] = display1;
                display2 = d2;
                return;
            case 3:
                d2 = copyOf(display2);
                d2[(_focus >> BITS1) & MASK] = display1;
                display2 = d2;
                Object[] d3 = copyOf(display3);
                d3[(_focus >> BITS2) & MASK] = d2;
                display3 = d3;
                return;
            case 4:
                d2 = copyOf(display2);
                d2[(_focus >> BITS1) & MASK] = display1;
                display2 = d2;
                d3 = copyOf(display3);
                d3[(_focus >> BITS2) & MASK] = d2;
                display3 = d3;
                Object[] d4 = copyOf(display4);
                d4[(_focus >> BITS3) & MASK] = d3;
                display4 = d4;
                return;
            case 5:
                d2 = copyOf(display2);
                d2[(_focus >> BITS1) & MASK] = display1;
                display2 = d2;
                d3 = copyOf(display3);
                d3[(_focus >> BITS2) & MASK] = d2;
                display3 = d3;
                d4 = copyOf(display4);
                d4[(_focus >> BITS3) & MASK] = d3;
                display4 = d4;
                Object[] d5 = copyOf(display5);
                d5[(_focus >> BITS4) & MASK] = d4;
                display5 = d5;
                return;
            case 6:
                d2 = copyOf(display2);
                d2[(_focus >> BITS1) & MASK] = display1;
                display2 = d2;
                d3 = copyOf(display3);
                d3[(_focus >> BITS2) & MASK] = d2;
                display3 = d3;
                d4 = copyOf(display4);
                d4[(_focus >> BITS3) & MASK] = d3;
                display4 = d4;
                d5 = copyOf(display5);
                d5[(_focus >> BITS4) & MASK] = d4;
                display5 = d5;
                Object[] createResult = copyOf(display6);
                createResult[(_focus >> BITS5) & MASK] = d5;
                display6 = createResult;
                return;
        }
    }

    // This method is not used in the original code
    void copyDisplaysTop(int level, int _focusRelax) {
        int _level = level;
        while (_level < depth) {
            int cutIndex;
            switch (_level) {
                case 2:
                    cutIndex = (_focusRelax >> BITS1) & MASK;
                    display2 = copyOf(display2, cutIndex + 1, cutIndex + 2);
                    break;
                case 3:
                    cutIndex = (_focusRelax >> BITS2) & MASK;
                    display3 = copyOf(display3, cutIndex + 1, cutIndex + 2);
                    break;
                case 4:
                    cutIndex = (_focusRelax >> BITS3) & MASK;
                    display4 = copyOf(display4, cutIndex + 1, cutIndex + 2);
                    break;
                case 5:
                    cutIndex = (_focusRelax >> BITS4) & MASK;
                    display5 = copyOf(display5, cutIndex + 1, cutIndex + 2);
                    break;
                case 6:
                    cutIndex = (_focusRelax >> BITS5) & MASK;
                    display6 = copyOf(display6, cutIndex + 1, cutIndex + 2);
                    break;
                default:
                    throw new AssertionError("Unexpected current depth: " + level);
            }
            _level += 1;
        }
    }

    protected void stabilizeDisplayPath(int _depth, int _focus) {
        if (_depth > 1) {
            display2[(_focus >> BITS1) & MASK] = display1;
            if (_depth > 2) {
                display3[(_focus >> BITS2) & MASK] = display2;
                if (_depth > 3) {
                    display4[(_focus >> BITS3) & MASK] = display3;
                    if (_depth > 4) {
                        display5[(_focus >> BITS4) & MASK] = display4;
                        if (_depth > 5) {
                            display6[(_focus >> BITS5) & MASK] = display5;
                        }
                    }
                }
            }
        }
    }

    protected Object @NotNull [] withRecomputeSizes(Object @NotNull [] node, int level, int branchToUpdate)
    {
        int nodeCount = node.length - 1;
        int[] oldSizes = getSizes(level, node);
        if (oldSizes != null) {
            int[] newSizes = new int[nodeCount];
            int delta = treeSize((Object[])node[branchToUpdate], level - 1);
            if (branchToUpdate > 0) {
                System.arraycopy(oldSizes, 0, newSizes, 0, branchToUpdate);
            }
            int i = branchToUpdate;
            while (i < nodeCount) {
                newSizes[i] = oldSizes[i] + delta;
                i += 1;
            }
            if (isNotBalanced(node, newSizes, level, nodeCount)) {
                installSizes(node, newSizes);
            } else {
                installSizes(node, null); // added to fix a bug
            }
        }
        return node;
    }

    protected Object @NotNull [] withComputedSizes(Object @NotNull [] node, int level)
    {
        assert level >= 2;
        int i = 0;
        int acc = 0;
        int nodeCount = node.length - 1;
        if (nodeCount > 1) {
            int[] sizes = new int[nodeCount];
            while (i < nodeCount) {
                acc += treeSize((Object[]) node[i], level - 1);
                sizes[i] = acc;
                i += 1;
            }
            if (isNotBalanced(node, sizes, level, nodeCount)) {
                node[nodeCount] = sizes;
            }
        } else if (nodeCount == 1 && level > 2) {
            Object[] child = (Object[]) node[0];
            int[] childSizes = getSizes(child);
            if (childSizes != null) {
                if (childSizes.length != 1) {
                    int[] sizes = new int[1];
                    sizes[0] = childSizes[childSizes.length - 1];
                    installSizes(node, sizes);
                } else {
                    installSizes(node, childSizes);
                }
            }
        }
        return node;
    }

    protected Object @NotNull [] withComputedSizes1(Object @NotNull [] node)
    {
        int i = 0;
        int acc = 0;
        int nodeCount = node.length - 1;
        if (nodeCount > 1) {
            int[] sizes = new int[nodeCount];
            while (i < nodeCount) {
                acc += ((Object[])node[i]).length;
                sizes[i] = acc;
                i += 1;
            }
            if /* node is not balanced */ (sizes[nodeCount - 2] != ((nodeCount - 1) << 5)) {
                installSizes(node, sizes);
            }
        }
        return node;
    }

    protected static int @NotNull [] makeTransientSizes(int @NotNull [] a, int transientBranchIndex)
    {
        int[] newSizes = new int[a.length];
        int delta = a[transientBranchIndex];
        if (transientBranchIndex > 0) {
            delta -= a[transientBranchIndex - 1];
            System.arraycopy(a, 0, newSizes, 0, transientBranchIndex);
        }
        int i = transientBranchIndex;
        int len = newSizes.length;
        while (i < len) {
            newSizes[i] = (a[i]) - delta;
            i += 1;
        }
        return newSizes;
    }

    protected boolean isNotBalanced(Object @NotNull [] node, int @NotNull [] sizes, int level, int end)
    {
        if (end == 1) {
            return true;
        }

        if (sizes[end - 2] != ((end - 1) << (5 * (level - 1)))) {
            return true;
        }

        if (level > 2) {
            Object[] last = (Object[]) node[end - 1];
            return last[last.length - 1] != null;
        }

        return false;
    }

    protected int treeSize(Object @NotNull [] node, int level)
    {
        return treeSizeRec(node, level, 0);
    }

    protected int treeSizeRec(Object @NotNull [] node, int level, int acc) {
        if (level == 1) {
            return acc + node.length;
        } else {
            int[] treeSizes = getSizes(node);
            if (treeSizes != null) {
                return acc + treeSizes[treeSizes.length - 1];
            } else {
                int fullNodeCount = node.length - 2;
                int fullNodesSize = fullNodeCount * getMaximumTreeSize(level - 1);
                return treeSizeRec((Object[]) node[fullNodeCount], level - 1, acc + fullNodesSize);
            }
        }
    }

    protected Object @NotNull [] getRoot()
    {
        Object[] root = getDisplay(depth);
        assert root != null;
        return root;
    }

    protected Object @Nullable [] getDisplay(int level)
    {
        switch (level) {
            case 1: return display1;
            case 2: return display2;
            case 3: return display3;
            case 4: return display4;
            case 5: return display5;
            case 6: return display6;
        }
        throw new UnsupportedOperationException("Unsupported level: " + level);
    }

    protected void setDisplay(int level, Object @Nullable [] display)
    {
        switch (level) {
            case 1: display1 = display; return;
            case 2: display2 = display; return;
            case 3: display3 = display; return;
            case 4: display4 = display; return;
            case 5: display5 = display; return;
            case 6: display6 = display; return;
        }
        throw new UnsupportedOperationException("Unsupported level: " + level);
    }
}
