/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.Visitor;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  Support for find methods that operates directly on the representation
*/

public final @Immutable class TreeSliceFinder
{
    public static @NotNull TreeSliceFinder get()
    {
        return INSTANCE;
    }

    private static final @NotNull TreeSliceFinder INSTANCE = new TreeSliceFinder();

    private TreeSliceFinder()
    {
    }

    public <V> void visit(@NotNull TreeSlices slices, int size, @NotNull Visitor<V> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = 0; sn < sliceCount; sn++) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            visitSlice(top, depth, visitor);
        }
    }

    public <V> void visitInRange(@NotNull TreeSlices slices, int start, int end, @NotNull Visitor<V> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = 0; sn < sliceCount; sn++) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            visitSliceInRange(start, end, top, depth, visitor);
        }
    }

    public <V> void visitReverse(@NotNull TreeSlices slices, int size, @NotNull Visitor<V> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = sliceCount-1; sn >= 0; sn--) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            visitSliceReverse(top, depth, visitor);
        }
    }

    public <V> void visitInRangeReverse(@NotNull TreeSlices slices, int start, int end, @NotNull Visitor<V> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = sliceCount-1; sn >= 0; sn--) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            visitSliceInRangeReverse(start, end, top, depth, visitor);
        }
    }

    private <V> void visitSlice(@NotNull Object[] top, int depth, @NotNull Visitor<V> visitor)
    {
        if (depth == 1) {
            for (Object element : top) {
                visitor.visit((V) element);
            }
        } else if (depth > 1) {
            for (Object o : top) {
                Object[] node = (Object[]) o;
                visitSlice(node, depth-1, visitor);
            }
        }
    }

    private <V> void visitSliceInRange(int start, int end, @NotNull Object[] top, int depth, @NotNull Visitor<V> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = 0; i < count; i++) {
                if (i >= start && i < end) {
                    Object element = top[i];
                    visitor.visit((V) element);
                }
            }
        } else if (depth > 1) {
            for (Object o : top) {
                Object[] node = (Object[]) o;
                visitSliceInRange(start, end, node, depth-1, visitor);
            }
        }
    }

    private <V> void visitSliceReverse(@NotNull Object[] top, int depth, @NotNull Visitor<V> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object element = top[i-1];
                visitor.visit((V) element);
            }
        } else if (depth > 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object[] node = (Object[]) top[i-1];
                visitSliceReverse(node, depth-1, visitor);
            }
        }
    }

    private <V> void visitSliceInRangeReverse(int start, int end, @NotNull Object[] top, int depth, @NotNull Visitor<V> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                if (i >= start && i < end) {
                    Object element = top[i - 1];
                    visitor.visit((V) element);
                }
            }
        } else if (depth > 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object[] node = (Object[]) top[i-1];
                visitSliceInRangeReverse(start, end, node, depth-1, visitor);
            }
        }
    }

    public <V,R> @Nullable R find(@NotNull TreeSlices slices, int size, @NotNull FindVisitor<V,R> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = 0; sn < sliceCount; sn++) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            R result = findInSlice(top, depth, visitor);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public <V,R> @Nullable R findInRange(@NotNull TreeSlices slices, int start, int end, @NotNull FindVisitor<V,R> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = 0; sn < sliceCount; sn++) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            R result = findInSlice(top, depth, visitor);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public <V,R> @Nullable R findReverse(@NotNull TreeSlices slices, int size, @NotNull FindVisitor<V,R> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = sliceCount-1; sn >= 0; sn--) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            R result = findInSliceReverse(top, depth, visitor);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public <V,R> @Nullable R findInRangeReverse(@NotNull TreeSlices slices, int start, int end, @NotNull FindVisitor<V,R> visitor)
    {
        int sliceCount = slices.getSliceCount();
        for (int sn = sliceCount-1; sn >= 0; sn--) {
            Object[] top = slices.getSlice(sn);
            int depth = slices.getSliceDepth(sn);
            R result = findInRangeInSliceReverse(start, end, top, depth, visitor);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private <V,R> @Nullable R findInSlice(@NotNull Object[] top, int depth, @NotNull FindVisitor<V,R> visitor)
    {
        if (depth == 1) {
            for (Object element : top) {
                R result = visitor.visit((V) element);
                if (result != null) {
                    return result;
                }
            }
        } else if (depth > 1) {
            for (Object o : top) {
                Object[] node = (Object[]) o;
                R result = findInSlice(node, depth-1, visitor);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private <V,R> @Nullable R findInRangeInSlice(int start, int end, @NotNull Object[] top, int depth, @NotNull FindVisitor<V,R> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = 0; i < count; i++) {
                if (i >= start && i < end) {
                    Object element = top[i];
                    R result = visitor.visit((V) element);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (depth > 1) {
            for (Object o : top) {
                Object[] node = (Object[]) o;
                R result = findInRangeInSlice(start, end, node, depth-1, visitor);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private <V,R> @Nullable R findInSliceReverse(@NotNull Object[] top, int depth, @NotNull FindVisitor<V,R> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object element = top[i-1];
                R result = visitor.visit((V) element);
                if (result != null) {
                    return result;
                }
            }
        } else if (depth > 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object[] node = (Object[]) top[i-1];
                R result = findInSliceReverse(node, depth-1, visitor);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private <V,R> @Nullable R findInRangeInSliceReverse(int start, int end, @NotNull Object[] top, int depth, @NotNull FindVisitor<V,R> visitor)
    {
        if (depth == 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                if (i >= start && i < end) {
                    Object element = top[i - 1];
                    R result = visitor.visit((V) element);
                    if (result != null) {
                        return result;
                    }
                }
            }
        } else if (depth > 1) {
            int count = top.length;
            for (int i = count; i > 0; i--) {
                Object[] node = (Object[]) top[i-1];
                R result = findInRangeInSliceReverse(start, end, node, depth-1, visitor);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
