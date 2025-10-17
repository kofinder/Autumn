package com.autumn.gc;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Allocator {
    private int nextAddress = 0;
    private final Queue<HeapObject> eden = new ConcurrentLinkedQueue<>();
    private final Queue<HeapObject> survivor = new ConcurrentLinkedQueue<>();
    private final Queue<HeapObject> oldGen = new ConcurrentLinkedQueue<>();
    private final MemoryManagementUnit mmu;
    private final int edenThreshold;
    public final int promotionAge;

    public Allocator(MemoryManagementUnit mmu, int edenThreshold, int promotionAge) {
        this.mmu = mmu;
        this.edenThreshold = edenThreshold;
        this.promotionAge = promotionAge;
    }

    public HeapObject allocate(int size, List<HeapObject> roots) {
        int edenSize = eden.stream().mapToInt(o -> o.size).sum();
        if (edenSize + size > edenThreshold)
            return null;

        HeapObject obj = new HeapObject(nextAddress, size);
        obj.generation = Generation.YOUNG;
        nextAddress += size;
        eden.add(obj);
        mmu.map("Eden", obj);
        roots.add(obj);
        return obj;
    }

    public List<HeapObject> getEden() {
        return new ArrayList<>(eden);
    }

    public List<HeapObject> getSurvivor() {
        return new ArrayList<>(survivor);
    }

    public List<HeapObject> getOldGen() {
        return new ArrayList<>(oldGen);
    }

    public void setEden(List<HeapObject> list) {
        eden.clear();
        eden.addAll(list);
    }

    public void setSurvivor(List<HeapObject> list) {
        survivor.clear();
        survivor.addAll(list);
    }

    public void setOldGen(List<HeapObject> list) {
        oldGen.clear();
        oldGen.addAll(list);
    }

    public int getTotalSize(List<HeapObject> list) {
        return list.stream().mapToInt(o -> o.size).sum();
    }

    public double getAverageSize(List<HeapObject> list) {
        return list.isEmpty() ? 0 : (double) getTotalSize(list) / list.size();
    }

    public int getTotalSizeByGeneration(List<HeapObject> list, Generation gen) {
        return list.stream().filter(o -> o.generation == gen).mapToInt(o -> o.size).sum();
    }

    public double getAverageSizeByGeneration(List<HeapObject> list, Generation gen) {
        List<HeapObject> filtered = list.stream().filter(o -> o.generation == gen).toList();
        if (filtered.isEmpty()) {
            return 0;
        }

        return (double) getTotalSizeByGeneration(filtered, gen) / filtered.size();
    }
}
