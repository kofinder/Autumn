package com.autumn.caches;

/**
 * Simulates compressed class pointer (CCP) optimization.
 * Stores references to ClassCache objects using a lightweight pointer
 * abstraction.
 */
public class CCP {

    private final ClassCache clazz;

    public CCP(ClassCache clazz) {
        this.clazz = clazz;
    }

    public ClassCache getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return "CCP{" + clazz.getClassName() + "}";
    }
}
