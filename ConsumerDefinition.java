package com.example.kafka;

import java.util.HashMap;
import java.util.Map;

public class ConsumerDefinition<K, V> {

    private String groupId;
    private boolean seekToBeginning = false;
    private boolean batch = false;

    private Class<K> keyClass; // if String don't use JsonDeserializer
    private Class<V> valueClass;

    private Map<String, Object> overrides = new HashMap<>();

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isSeekToBeginning() {
        return seekToBeginning;
    }

    public void setSeekToBeginning(boolean seekToBeginning) {
        this.seekToBeginning = seekToBeginning;
    }

    public boolean isBatch() {
        return batch;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public Class<K> getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(Class<K> keyClass) {
        this.keyClass = keyClass;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    public Map<String, Object> getOverrides() {
        return overrides;
    }

    public void setOverrides(Map<String, Object> overrides) {
        this.overrides = overrides;
    }
}
