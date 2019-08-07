package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaConsumer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaConsumer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Component
    public static class Consumer {

        @KafkaListener(topics = "first", containerFactory = "kafkaListenerContainerFactory")
        public void listen(MyRecord record) {
            if (record.getValue() > 5) {
                throw new RuntimeException("bad value: " + record.getValue());
            }
            System.out.println(record);
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
        public ConcurrentKafkaListenerContainerFactory<String, MyRecord> kafkaListenerContainerFactory() {
            DefaultKafkaConsumerFactory<String, MyRecord> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs());
            consumerFactory.setKeyDeserializer(new StringDeserializer());
            consumerFactory.setValueDeserializer(new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(MyRecord.class)));
            ConcurrentKafkaListenerContainerFactory<String, MyRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
//            factory.getContainerProperties().setAckOnError(false);
//            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
            factory.setErrorHandler(new CustomKafkaErrorHandler(errorTemplate(), deserrTemplate()));
            factory.setStatefulRetry(true);
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
