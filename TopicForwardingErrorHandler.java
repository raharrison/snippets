package com.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.record.DefaultRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class TopicForwardingErrorHandler implements ContainerAwareErrorHandler {

    private ObjectMapper mapper = new ObjectMapper();

    private ContainerAwareErrorHandler appExceptionDelegate;
    private ContainerAwareErrorHandler deserializationExceptionDelegate;

    public TopicForwardingErrorHandler(KafkaTemplate<Object, Object> errorTemplate, KafkaTemplate<String, String> deserrTemplate) {
        appExceptionDelegate = new SeekToCurrentErrorHandler(ThrowingErrorTopicPublishingRecoverer.buildErrorPublisher(errorTemplate,
                (cr, e) -> new TopicPartition(cr.topic() + ".error", 0)), 1);

        deserializationExceptionDelegate = new SeekToCurrentErrorHandler(ThrowingErrorTopicPublishingRecoverer.buildDeserializationPublisher(deserrTemplate,
                (cr, e) -> new TopicPartition(cr.topic() + ".deserr", 0)), 1);
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer) {
        try {
            if (thrownException instanceof DeserializationException) {
                List<ConsumerRecord<?, ?>> processedRecords = processDeserializationError(records);
                deserializationExceptionDelegate.handle(thrownException, processedRecords, consumer);
            } else {
                appExceptionDelegate.handle(thrownException, records, consumer);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Exception handling error", e);
        }
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        try {
            if (thrownException instanceof DeserializationException) {
                List<ConsumerRecord<?, ?>> processedRecords = processDeserializationError(records);
                for (ConsumerRecord<?, ?> rec : processedRecords) {
                    System.out.println("Offset = " + rec.offset());
                    System.out.println("Value = " + rec.value());
                }
                System.out.println("-------------");
                deserializationExceptionDelegate.handle(thrownException, processedRecords, consumer, container);
            } else {
                appExceptionDelegate.handle(thrownException, records, consumer, container);
            }
        } catch (Exception e) {
            System.out.println("Exception handling error, pausing container...");
            container.pause();
            throw new RuntimeException(e);
        }
    }

    private List<ConsumerRecord<?, ?>> processDeserializationError(List<ConsumerRecord<?, ?>> records) throws Exception {
        List<ConsumerRecord<?, ?>> newRecords = new ArrayList<>(records.size());
        for (ConsumerRecord<?, ?> record : records) {
            Header keyErrorHeader = record.headers().lastHeader(ErrorHandlingDeserializer2.KEY_DESERIALIZER_EXCEPTION_HEADER);
            Header valueErrorHeader = record.headers().lastHeader(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_EXCEPTION_HEADER);
            if (keyErrorHeader != null || valueErrorHeader != null) {
                newRecords.add(constructErrorRecord(record, keyErrorHeader, valueErrorHeader));
            } else {
                newRecords.add(record);
            }
        }
        return newRecords;
    }

    private ConsumerRecord<String, String> constructErrorRecord(ConsumerRecord<?, ?> record, Header keyHeader, Header valueHeader) throws Exception {
        String key, value;
        if (keyHeader == null) {
            key = toJsonString(record.key());
        } else {
            key = retrieveRawValue(keyHeader);
        }

        if (valueHeader == null) {
            value = toJsonString(record.value());
        } else {
            value = retrieveRawValue(valueHeader);
        }

        int keySize = key == null ? 0 : key.getBytes().length;
        int valueSize = value == null ? 0 : value.getBytes().length;
        long checksum = computeChecksum(record.timestamp(), keySize, valueSize);

        return new ConsumerRecord<>(record.topic(), record.partition(), record.offset(), record.timestamp(), record.timestampType(),
                checksum, keySize, valueSize, key, value, record.headers());
    }

    private String retrieveRawValue(Header errorHeader) throws Exception {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(errorHeader.value());
             ObjectInputStream oos = new ObjectInputStream(stream)) {
            DeserializationException deserializationException = (DeserializationException) oos.readObject();
            return new String(deserializationException.getData());
        }
    }

    private String toJsonString(Object value) throws JsonProcessingException {
        if (value == null) {
            return null;
        }
        return mapper.writeValueAsString(value);
    }

    private long computeChecksum(long timestamp, int serializedKeySize, int serializedValueSize) {
        return DefaultRecord.computePartialChecksum(timestamp, serializedKeySize, serializedValueSize);
    }
}
