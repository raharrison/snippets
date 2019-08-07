package com.example.kafka;

import java.util.Objects;

public class MyRecord {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyRecord myRecord = (MyRecord) o;
        return value == myRecord.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "MyRecord{" +
                "value=" + value +
                '}';
    }
}
