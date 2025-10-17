package com.autumn.gc;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Collector implements Runnable {
    private final Allocator allocator;
    private final List<HeapObject> roots;
    private final MemoryManagementUnit mmu;
    private volatile boolean running = true;
    private final ExecutorService youngPool;
    private final ExecutorService oldPool;
    private final int gcThreads;

    // Metrics
    public AtomicInteger totalPromoted = new AtomicInteger();
    public AtomicInteger totalReclaimed = new AtomicInteger();
    public AtomicInteger gcEvents = new AtomicInteger();
    public AtomicInteger totalSTWTime = new AtomicInteger();

    public Collector(Allocator allocator, MemoryManagementUnit mmu, List<HeapObject> roots, int gcThreads) {
        this.allocator = allocator;
        this.mmu = mmu;
        this.roots = roots;
        this.gcThreads = gcThreads;
        this.youngPool = Executors.newFixedThreadPool(gcThreads);
        this.oldPool = Executors.newFixedThreadPool(gcThreads);
    }

    public void stop() {
        running = false;
        youngPool.shutdown();
        oldPool.shutdown();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long stwStart = System.currentTimeMillis();
            stopTheWorldMark();
            long stwDuration = System.currentTimeMillis() - stwStart;
            totalSTWTime.addAndGet((int) stwDuration);

            logGCEvent("STW Mark Phase completed", stwDuration);
            parallelYoungGC();
            parallelOldGC();
            logLiveHeap();
        }
    }

    // === Stop-the-world mark ===
    private void stopTheWorldMark() {
        synchronized (allocator) {
            Set<HeapObject> visited = new HashSet<>();
            for (HeapObject root : roots)
                traverse(root, visited);
        }
    }

    private void traverse(HeapObject obj, Set<HeapObject> visited) {
        if (obj == null || visited.contains(obj))
            return;
        visited.add(obj);
        obj.marked = true;
        for (HeapObject ref : obj.references)
            traverse(ref, visited);
    }

    // === Parallel Young GC ===
    private void parallelYoungGC() {
        List<HeapObject> edenSnapshot = allocator.getEden();
        List<HeapObject> survivor = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger promoted = new AtomicInteger();
        AtomicInteger reclaimed = new AtomicInteger();
        int promotionAge = allocator.promotionAge;

        int chunkSize = Math.max(1, edenSnapshot.size() / gcThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < edenSnapshot.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, edenSnapshot.size());
            tasks.add(() -> {
                for (int j = start; j < end; j++) {
                    HeapObject obj = edenSnapshot.get(j);
                    if (obj.marked) {
                        obj.age++;
                        if (obj.age >= promotionAge) {
                            allocator.getOldGen().add(obj);
                            mmu.map("OldGen", obj);
                            promoted.incrementAndGet();
                        } else {
                            survivor.add(obj);
                            mmu.map("Survivor", obj);
                        }
                        obj.marked = false;
                    } else {
                        reclaimed.incrementAndGet();
                    }
                }
                return null;
            });
        }

        try {
            youngPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        allocator.setEden(new ArrayList<>());
        allocator.setSurvivor(survivor);

        totalPromoted.addAndGet(promoted.get());
        totalReclaimed.addAndGet(reclaimed.get());

        logHeapUsage("Young GC Event#" + gcEvents.incrementAndGet(), edenSnapshot.size(), survivor.size(),
                allocator.getOldGen().size(), promoted.get(), reclaimed.get());
    }

    // === Parallel Old GC ===
    private void parallelOldGC() {
        List<HeapObject> oldSnapshot = allocator.getOldGen();
        List<HeapObject> compacted = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger reclaimed = new AtomicInteger();

        int chunkSize = Math.max(1, oldSnapshot.size() / gcThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < oldSnapshot.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, oldSnapshot.size());
            tasks.add(() -> {
                for (int j = start; j < end; j++) {
                    HeapObject obj = oldSnapshot.get(j);
                    if (obj.marked) {
                        compacted.add(obj);
                        obj.marked = false;
                    } else {
                        reclaimed.incrementAndGet();
                    }
                }
                return null;
            });
        }

        try {
            oldPool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Compact addresses sequentially
        int addr = 0;
        for (HeapObject obj : compacted) {
            obj.address = addr;
            addr += obj.size;
        }
        allocator.setOldGen(compacted);

        totalReclaimed.addAndGet(reclaimed.get());
        logHeapUsage("Old GC Event#" + gcEvents.incrementAndGet(), allocator.getEden().size(),
                allocator.getSurvivor().size(), allocator.getOldGen().size(), 0, reclaimed.get());
    }

    private void logGCEvent(String message, long stwTime) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println("[" + timestamp + "] GC: " + message + " | STW=" + stwTime + "ms");
    }

    private void logHeapUsage(String event, int edenSize, int survivorSize, int oldGenSize, int promoted,
            int reclaimed) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.printf("[%s] %s | Eden=%d | Survivor=%d | OldGen=%d | Promoted=%d | Reclaimed=%d%n",
                timestamp, event, edenSize, survivorSize, oldGenSize, promoted, reclaimed);
    }

    private void logLiveHeap() {
        System.out.printf(">>> Live Heap Counts: Eden=%d | Survivor=%d | OldGen=%d%n",
                allocator.getEden().size(), allocator.getSurvivor().size(), allocator.getOldGen().size());
    }
}
