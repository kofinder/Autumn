package com.autumn.caches;

import com.autumn.gc.HeapObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates loaded class metadata storage.
 * Each class may hold references to static objects.
 */
public class ClassCache {

    private final Map<String, HeapObject> classStatics = new ConcurrentHashMap<>();

    private final String className;

    public ClassCache(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void addStaticField(String fieldName, HeapObject obj) {
        classStatics.put(fieldName, obj);
    }

    public HeapObject getStaticField(String fieldName) {
        return classStatics.get(fieldName);
    }

    public Collection<HeapObject> getStaticReferences() {
        return classStatics.values();
    }

    @Override
    public String toString() {
        return "ClassCache{" + className + ", statics=" + classStatics.size() + "}";
    }
}
