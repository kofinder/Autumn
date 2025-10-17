package com.autumn.caches;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates JVM Metaspace memory.
 * Stores class metadata, constant pools, and static data.
 */
public class Metaspace {

    private final Map<String, ClassCache> loadedClasses = new ConcurrentHashMap<>();
    private int totalSize = 0;

    public void addClass(ClassCache clazz, int size) {
        if (!loadedClasses.containsKey(clazz.getClassName())) {
            loadedClasses.put(clazz.getClassName(), clazz);
            totalSize += size;
        }
    }

    public ClassCache getClass(String className) {
        return loadedClasses.get(className);
    }

    public Collection<ClassCache> getAllClasses() {
        return loadedClasses.values();
    }

    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public String toString() {
        return "Metaspace{classes=" + loadedClasses.size() + ", totalSize=" + totalSize + "}";
    }
}
