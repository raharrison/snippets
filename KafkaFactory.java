package com.example.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerStoppingBatchErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KafkaFactory {

    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

//            props.put(ProducerConfig.RETRIES_CONFIG, 3);
//            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15_000);
//            props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);
        return props;
    }

    public KafkaTemplate<Object, Object> createJsonKafkaTemplate() {
        ProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig(),
                new JsonSerializer<>(), new JsonSerializer<>());
        return new KafkaTemplate<>(producerFactory);
    }

    public KafkaTemplate<String, String> createStringKafkaTemplate() {
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig(),
                new StringSerializer(), new StringSerializer());
        return new KafkaTemplate<>(producerFactory);
    }

    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> createListenerContainerFactory(ConsumerDefinition<K, V> definition,
                                                                                               KafkaTemplate<Object, Object> errorTemplate,
                                                                                               KafkaTemplate<String, String> deserrTemplate) {
        ConsumerFactory<K, V> consumerFactory = createConsumerFactory(definition);
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setErrorHandler(new TopicForwardingErrorHandler(errorTemplate, deserrTemplate));
        factory.setStatefulRetry(true);
        if (definition.isSeekToBeginning()) {
            addSeekToBeginningListener(factory);
        }
        return factory;
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> createBatchListenerContainerFactory(ConsumerDefinition<String, String> definition) {
        ConsumerFactory<String, String> consumerFactory = createConsumerFactory(definition);
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        factory.setBatchListener(true);
        factory.setBatchErrorHandler(new ContainerStoppingBatchErrorHandler());
        if (definition.isSeekToBeginning()) {
            addSeekToBeginningListener(factory);
        }
        return factory;
    }

    private <K, V> ConsumerFactory<K, V> createConsumerFactory(ConsumerDefinition<K, V> definition) {
        Map<String, Object> props = consumerConfig();
        if (definition.getGroupId() == null) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        } else {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, definition.getGroupId());
        }
        props.putAll(definition.getOverrides());
        DefaultKafkaConsumerFactory<K, V> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        consumerFactory.setKeyDeserializer(createDeserializer(definition.getKeyClass(), true));
        consumerFactory.setValueDeserializer(createDeserializer(definition.getValueClass(), false));
        return consumerFactory;
    }

    private void addSeekToBeginningListener(ConcurrentKafkaListenerContainerFactory factory) {
        factory.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() {
            @Override
            public void onPartitionsAssigned(org.apache.kafka.clients.consumer.Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                consumer.seekToBeginning(partitions);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> ErrorHandlingDeserializer2<T> createDeserializer(Class<T> clazz, boolean isKey) {
        ErrorHandlingDeserializer2<T> deserializer;
        if (clazz.equals(String.class)) {
            deserializer = (ErrorHandlingDeserializer2<T>) new ErrorHandlingDeserializer2<>(new StringDeserializer());
        } else {
            deserializer = new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(clazz));
        }
        deserializer.setKey(isKey);
        return deserializer;
    }

}
