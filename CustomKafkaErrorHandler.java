package com.example.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.record.DefaultRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomKafkaErrorHandler implements ContainerAwareErrorHandler {

    private ContainerAwareErrorHandler appExceptionDelegate;
    private ContainerAwareErrorHandler deserializationExceptionDelegate;

    public CustomKafkaErrorHandler(KafkaTemplate<Object, Object> errorTemplate, KafkaTemplate<Object, Object> deserrTemplate) {
        appExceptionDelegate = new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(errorTemplate,
                (cr, e) -> new TopicPartition(cr.topic() + ".error", 0)), 2);

        deserializationExceptionDelegate = new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(deserrTemplate,
                (cr, e) -> new TopicPartition(cr.topic() + ".deserr", 0)), 1);
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer) {
        if (thrownException instanceof DeserializationException) {
            deserializationExceptionDelegate.handle(thrownException, records, consumer);
        } else {
            appExceptionDelegate.handle(thrownException, records, consumer);
        }
    }

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        if (thrownException instanceof DeserializationException) {
            List<ConsumerRecord<?, ?>> consumerRecords = processDesError(records);
            deserializationExceptionDelegate.handle(thrownException, consumerRecords, consumer, container);
        } else {
            appExceptionDelegate.handle(thrownException, records, consumer, container);
        }
    }

    private List<ConsumerRecord<?, ?>> processDesError(List<ConsumerRecord<?, ?>> records) {
        List<ConsumerRecord<?, ?>> newRecords = new ArrayList<>(records.size());
        for (ConsumerRecord<?, ?> record : records) {
            Header keyErrorHeader = record.headers().lastHeader(ErrorHandlingDeserializer2.KEY_DESERIALIZER_EXCEPTION_HEADER);
            Header valueErrorHeader = record.headers().lastHeader(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_EXCEPTION_HEADER);
            if (keyErrorHeader != null) {
                newRecords.add(constructErrorRecord(record, keyErrorHeader));
            } else if (valueErrorHeader != null) {
                newRecords.add(constructErrorRecord(record, valueErrorHeader));
            } else {
                newRecords.add(record);
            }
        }
        return newRecords;
    }

    private ConsumerRecord<?, ?> constructErrorRecord(ConsumerRecord<?, ?> record, Header errorHeader) {
        ByteArrayInputStream stream = new ByteArrayInputStream(errorHeader.value());
        try (ObjectInputStream oos = new ObjectInputStream(stream)) {
            DeserializationException deserializationException = (DeserializationException) oos.readObject();
            String rawValue = new String(deserializationException.getData());
            if (deserializationException.isKey()) {
                long checksum = computeChecksum(record.timestamp(), deserializationException.getData().length, record.serializedValueSize());
                return new ConsumerRecord<>(record.topic(), record.partition(), record.offset(), record.timestamp(), record.timestampType(),
                        checksum, deserializationException.getData().length, record.serializedValueSize(), rawValue, record.value(), record.headers());
            } else {
                long checksum = computeChecksum(record.timestamp(), record.serializedKeySize(), deserializationException.getData().length);
                return new ConsumerRecord<>(record.topic(), record.partition(), record.offset(), record.timestamp(), record.timestampType(),
                        checksum, record.serializedKeySize(), deserializationException.getData().length, record.key(), rawValue, record.headers());
            }
        } catch (Exception ex2) {
            throw new IllegalStateException("Could not deserialize Exception from stream", ex2);
        }
    }

    private long computeChecksum(long timestamp, int serializedKeySize, int serializedValueSize) {
        return DefaultRecord.computePartialChecksum(timestamp, serializedKeySize, serializedValueSize);
    }
}
