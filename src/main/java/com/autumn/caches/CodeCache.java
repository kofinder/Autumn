package com.autumn.caches;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates JIT-compiled code cache.
 * Stores compiled methods and their memory footprint.
 */
public class CodeCache {

    private final Map<String, Integer> compiledMethods = new ConcurrentHashMap<>();

    private int totalSize = 0;

    /**
     * Add a compiled method with its size in bytes.
     */
    public void addCompiledMethod(String methodName, int size) {
        if (!compiledMethods.containsKey(methodName)) {
            compiledMethods.put(methodName, size);
            totalSize += size;
        }
    }

    /**
     * Remove a compiled method (simulate code invalidation).
     */
    public void removeCompiledMethod(String methodName) {
        Integer size = compiledMethods.remove(methodName);
        if (size != null)
            totalSize -= size;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public Collection<String> getMethods() {
        return compiledMethods.keySet();
    }

    @Override
    public String toString() {
        return "CodeCache{methods=" + compiledMethods.size() + ", totalSize=" + totalSize + "}";
    }
}
