package com.example.kafka;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaConsumer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Component
    public static class Consumer {

        @KafkaListener(topics = "eight", containerFactory = "kafkaListenerContainerFactory")
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
        public ConcurrentKafkaListenerContainerFactory<MyRecord, MyRecord> kafkaListenerContainerFactory() {
            ConsumerDefinition<MyRecord, MyRecord> definition = new ConsumerDefinition<>();
            definition.setKeyClass(MyRecord.class);
            definition.setValueClass(MyRecord.class);
            definition.setSeekToBeginning(true);
            return kafkaFactory().createListenerContainerFactory(definition, jsonKafkaTemplate(), stringKafkaTemplate());
        }

        @Bean
        public KafkaFactory kafkaFactory() {
            return new KafkaFactory();
        }

        @Bean
        public KafkaTemplate<Object, Object> jsonKafkaTemplate() {
            return kafkaFactory().createJsonKafkaTemplate();
        }

        @Bean
        public KafkaTemplate<String, String> stringKafkaTemplate() {
            return kafkaFactory().createStringKafkaTemplate();
        }

    }
}
