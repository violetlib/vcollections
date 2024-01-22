/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

/**
  Options to select a list implementation based on usage.
*/

public enum ListUsage
{
    DEFAULT,    // a default implementation, adequately supporting access and extending
    ACCESS,     // optimized for efficient access, not optimized for extending
    PREPEND,    // optimized for prepending
    APPEND,     // optimized for appending
}
