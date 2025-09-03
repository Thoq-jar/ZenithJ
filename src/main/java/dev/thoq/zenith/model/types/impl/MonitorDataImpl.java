package dev.thoq.zenith.model.types.impl;

import dev.thoq.zenith.model.types.MonitorData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"NullableProblems", "EqualsDoesntCheckParameterClass"})
public record MonitorDataImpl(Map<String, Map<String, Double>> data) implements MonitorData {
    public MonitorDataImpl(Map<String, Map<String, Double>> data) {
        this.data = new HashMap<>(data);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public Map<String, Double> get(Object key) {
        return data.get(key);
    }

    @Override
    public Map<String, Double> put(String key, Map<String, Double> value) {
        return data.put(key, value);
    }

    @Override
    public Map<String, Double> remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Map<String, Double>> m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<Map<String, Double>> values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, Map<String, Double>>> entrySet() {
        return data.entrySet();
    }

    @Override
    public boolean equals(Object obj) {
        return data.equals(obj);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
