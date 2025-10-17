package com.autumn.gc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HeapObject {

    public int address;
    public int size;
    public int age = 0;
    public boolean marked = false;
    public Generation generation = Generation.YOUNG;
    public List<HeapObject> references = new CopyOnWriteArrayList<>();

    public HeapObject(int address, int size) {
        this.address = address;
        this.size = size;
    }

    public void addReference(HeapObject obj) {
        references.add(obj);
    }

    @Override
    public String toString() {
        return "Object@" + address + " size=" + size + " age=" + age + " gen=" + generation;
    }

}
