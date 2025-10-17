package com.autumn.gc;

import java.util.*;
import java.util.concurrent.*;

public class FuckGC {
    public static void main(String[] args) throws InterruptedException {
        int edenThreshold = 1024 * 1024; // 1MB for Eden
        int promotionAge = 2; // objects promoted after 2 GC cycles
        int gcThreads = 4; // threads for parallel GC
        int mutatorThreads = 3; // number of mutators

        var mmu = new MemoryManagementUnit(); // implement mapping logic
        var allocator = new Allocator(mmu, edenThreshold, promotionAge);
        List<HeapObject> roots = Collections.synchronizedList(new ArrayList<>());

        var collector = new Collector(allocator, mmu, roots, gcThreads);
        var gcThread = new Thread(collector, "GC-Thread");
        gcThread.start();

        var mutators = Executors.newFixedThreadPool(mutatorThreads);
        for (int i = 0; i < mutatorThreads; i++) {
            mutators.submit(new Mutator(allocator, roots));
        }

        Thread.sleep(10000); // run for 10 seconds

        System.out.println("Stopping mutators and GC...");
        mutators.shutdownNow();
        collector.stop();
        gcThread.join();

        System.out.printf(">>> Final Heap Counts: Eden=%d | Survivor=%d | OldGen=%d%n",
                allocator.getEden().size(), allocator.getSurvivor().size(), allocator.getOldGen().size());
        System.out.printf("Total Promoted=%d | Total Reclaimed=%d | GC Events=%d | Total STW Time=%dms%n",
                collector.totalPromoted.get(), collector.totalReclaimed.get(),
                collector.gcEvents.get(), collector.totalSTWTime.get());
    }
}
