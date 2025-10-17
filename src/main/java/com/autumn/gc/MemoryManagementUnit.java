package com.autumn.gc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryManagementUnit {

    private Map<String, List<HeapObject>> heapMap = new ConcurrentHashMap<>();

    public void map(String space, HeapObject obj) {
        heapMap.computeIfAbsent(space, k -> Collections.synchronizedList(new ArrayList<>())).add(obj);
    }

    public void printMappings() {
        System.out.println("\n=== MMU: Heap Spaces ===");
        heapMap.forEach((space, objs) -> System.out.println(space + ": " + objs));
    }

}
