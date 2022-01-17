package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import java.util.function.BiFunction;

public class ThrowingErrorTopicPublishingRecoverer extends DeadLetterPublishingRecoverer {

    private ThrowingErrorTopicPublishingRecoverer(KafkaTemplate<Object, Object> template, BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver) {
        super(template, destinationResolver);
    }

    public static ThrowingErrorTopicPublishingRecoverer buildErrorPublisher(KafkaTemplate<Object, Object> template, BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver) {
        return new ThrowingErrorTopicPublishingRecoverer(template, destinationResolver);
    }

    @SuppressWarnings("unchecked")
    public static ThrowingErrorTopicPublishingRecoverer buildDeserializationPublisher(KafkaTemplate<String, String> template, BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver) {
        KafkaTemplate<Object, Object> objTemplate = (KafkaTemplate<Object, Object>) (Object) template;
        return new ThrowingErrorTopicPublishingRecoverer(objTemplate, destinationResolver);
    }

    @Override
    protected void publish(ProducerRecord<Object, Object> outRecord, KafkaOperations<Object, Object> kafkaTemplate) {
        try {
            kafkaTemplate.send(outRecord).get();
        } catch (Exception e) {
            throw new RuntimeException("Exception publishing to error topic: " + outRecord, e);
        }
    }
}
