package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

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
        private KafkaTemplate<String, String> deserrTemplate;

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
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
            ConsumerDefinition<String, String> definition = new ConsumerDefinition<>();
            return kafkaFactory().createBatchListenerContainerFactory(definition);
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
