package com.autumn.caches;

import com.autumn.gc.HeapObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all JVM caches: ClassCache, CodeCache, Metaspace, CCP.
 * Provides APIs to allocate, lookup, and track cache objects.
 */
public class CacheManager {

    private final Metaspace metaspace = new Metaspace();
    private final CodeCache codeCache = new CodeCache();
    private final Map<String, CCP> ccpMap = new ConcurrentHashMap<>();

    /**
     * Load a class into Metaspace and return its ClassCache.
     */
    public ClassCache loadClass(String className, int size) {
        ClassCache clazz = new ClassCache(className);
        metaspace.addClass(clazz, size);
        CCP ccp = new CCP(clazz);
        ccpMap.put(className, ccp);
        return clazz;
    }

    public CCP getCCP(String className) {
        return ccpMap.get(className);
    }

    public void compileMethod(String className, String methodName, int size) {
        codeCache.addCompiledMethod(className + "." + methodName, size);
    }

    public void removeCompiledMethod(String className, String methodName) {
        codeCache.removeCompiledMethod(className + "." + methodName);
    }

    public Collection<ClassCache> getAllClasses() {
        return metaspace.getAllClasses();
    }

    public Collection<String> getAllCompiledMethods() {
        return codeCache.getMethods();
    }

    public Collection<HeapObject> getAllStaticRoots() {
        List<HeapObject> roots = new ArrayList<>();
        for (ClassCache clazz : metaspace.getAllClasses()) {
            roots.addAll(clazz.getStaticReferences());
        }
        return roots;
    }

    @Override
    public String toString() {
        return "CacheManager{Metaspace=" + metaspace.getAllClasses().size() +
                ", CodeCache=" + codeCache.getMethods().size() +
                ", CCP=" + ccpMap.size() + "}";
    }
}
