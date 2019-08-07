package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerStoppingBatchErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaBatchConsumer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaBatchConsumer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Component
    public static class Consumer {

        @Autowired
        private KafkaTemplate<Object, Object> errorTemplate;

        @Autowired
        private KafkaTemplate<Object, Object> deserrTemplate;

        @KafkaListener(topics = "first", containerFactory = "kafkaListenerContainerFactory")
        public void listen(List<ConsumerRecord<String, String>> records) {
            BatchConsumer<String, MyRecord> consumer = new BatchConsumer<>(String.class, MyRecord.class, errorTemplate, deserrTemplate) {
                @Override
                public void process(ConsumerRecord<String, MyRecord> record) {
                    if (record.value().getValue() > 5) {
                        throw new RuntimeException("bad value: " + record.value().getValue());
                    }
                    System.out.println(record);
                }
            };
            consumer.processRecords(records);
        }
    }

    @EnableKafka
    @Configuration
    public static class Config {

        @Bean
        public Map<String, Object> consumerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return props;
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
            DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs());
            consumerFactory.setKeyDeserializer(new StringDeserializer());
            consumerFactory.setValueDeserializer(new StringDeserializer());
            ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.setBatchListener(true);
            factory.setBatchErrorHandler(new ContainerStoppingBatchErrorHandler());
            return factory;
        }

        @Bean
        public KafkaTemplate<Object, Object> errorTemplate() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            ProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        public KafkaTemplate<Object, Object> deserrTemplate() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            ProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
            return new KafkaTemplate<>(producerFactory);
        }

    }
}
