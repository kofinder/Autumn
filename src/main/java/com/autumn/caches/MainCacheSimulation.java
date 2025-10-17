package com.autumn.caches;

import com.autumn.gc.HeapObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MainCacheSimulation {

    public static void main(String[] args) {
        CacheManager cacheManager = new CacheManager();

        ThreadLocalRandom rand = ThreadLocalRandom.current();

        System.out.println("Loading classes into Metaspace...");
        List<ClassCache> loadedClasses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ClassCache clazz = cacheManager.loadClass("com.example.Class" + i, rand.nextInt(1024, 4096));
            loadedClasses.add(clazz);

            for (int j = 1; j <= 3; j++) {
                HeapObject staticObj = new HeapObject(rand.nextInt(1000), rand.nextInt(64, 128));
                clazz.addStaticField("staticField" + j, staticObj);
            }

            for (int k = 1; k <= 2; k++) {
                cacheManager.compileMethod(clazz.getClassName(), "method" + k, rand.nextInt(128, 512));
            }
        }

        System.out.println("\n--- Cache Status After Loading ---");
        System.out.println(cacheManager);

        Collection<HeapObject> staticRoots = cacheManager.getAllStaticRoots();
        System.out.println("\nCollected Static Roots for GC:");
        for (HeapObject obj : staticRoots) {
            System.out.println(obj);
        }

        System.out.println("\nInvalidating a compiled method...");
        ClassCache targetClass = loadedClasses.get(0);
        cacheManager.removeCompiledMethod(targetClass.getClassName(), "method1");

        System.out.println("\n--- Final Cache Status ---");
        System.out.println(cacheManager);

        System.out.printf("\nTotal static roots: %d\n", staticRoots.size());
    }
}
