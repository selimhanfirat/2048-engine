package ai.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoardLRUCache<K, v> extends LinkedHashMap<K, v> {
    private final int capacity;

    public BoardLRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, v> eldest) {
        return size() > capacity;
    }
}
