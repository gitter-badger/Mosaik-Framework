package io.github.splotycode.mosaik.util.collection;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;

    public MaxSizeHashMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

}
