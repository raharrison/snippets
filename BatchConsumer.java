package com.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BatchConsumer<K, V> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Class<K> kClass;
    private Class<V> vClass;
    private KafkaTemplate<Object, Object> errorTemplate;
    private KafkaTemplate<String, String> deserrTemplate;

    public BatchConsumer(Class<K> kClass, Class<V> vClass, KafkaTemplate<Object, Object> errorTemplate, KafkaTemplate<String, String> deserrTemplate) {
        this.kClass = kClass;
        this.vClass = vClass;
        this.errorTemplate = errorTemplate;
        this.deserrTemplate = deserrTemplate;
    }

    private ConsumerRecord<K, V> deserializeOrDeadLetter(ConsumerRecord<String, String> record) {
        K key;
        V value;
        try {
            key = record.key() == null ? null : objectMapper.readValue(record.key(), kClass);
            value = record.value() == null ? null : objectMapper.readValue(record.value(), vClass);
            return new ConsumerRecord<>(record.topic(), record.partition(), record.offset(), key, value);
        } catch (IOException e) {
            System.out.println("Error deserializing: " + record.value());
            try {
                deserrTemplate.send(record.topic() + ".deserr", 0, record.key(), record.value()).get();
                return null;
            } catch (Exception ex) {
                throw new RuntimeException("Exception pushing to error topic", ex);
            }
        }
    }

    public abstract void process(ConsumerRecord<K, V> record);

    public void processRecords(List<ConsumerRecord<String, String>> records) {
        List<ConsumerRecord<K, V>> deserializedRecords = records.
                stream()
                .map(this::deserializeOrDeadLetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (ConsumerRecord<K, V> deserializedRecord : deserializedRecords) {
            try {
                process(deserializedRecord);
            } catch (Exception e) {
                System.out.println("Error processing: " + deserializedRecord.value());
                try {
                    errorTemplate.send(deserializedRecord.topic() + ".error", 0, deserializedRecord.key(), deserializedRecord.value()).get();
                } catch (Exception ex) {
                    throw new RuntimeException("Exception pushing to error topic", ex);
                }
            }
        }
    }
}
