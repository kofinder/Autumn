package com.autumn.gc;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Mutator implements Runnable {
    private final Allocator allocator;
    private final List<HeapObject> roots; // must be synchronized

    public Mutator(Allocator allocator, List<HeapObject> roots) {
        this.allocator = allocator;
        // wrap roots in a synchronizedList to make add/remove safe
        this.roots = Collections.synchronizedList(roots);
    }

    @Override
    public void run() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < 30; i++) {
            int size = 16 + rand.nextInt(64);
            HeapObject obj = allocator.allocate(size, roots);

            if (obj != null) {
                // randomly add references
                synchronized (roots) {
                    if (!roots.isEmpty() && rand.nextBoolean()) {
                        HeapObject target = roots.get(rand.nextInt(roots.size()));
                        obj.addReference(target);
                    }
                }
            }

            // randomly remove some roots
            synchronized (roots) {
                if (!roots.isEmpty() && rand.nextBoolean()) {
                    roots.remove(rand.nextInt(roots.size()));
                }
            }

            try {
                Thread.sleep(100); // simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
