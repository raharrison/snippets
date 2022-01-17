package com.example.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class KafkaProducer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(KafkaProducer.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Component
    public static class Producer {

        @Autowired
        private KafkaTemplate<Object, Object> kafkaTemplate;

        @PostConstruct
        public void init() throws Exception {
            System.out.println(kafkaTemplate == null);
//            kafkaTemplate.send("first", "key", "value").get();
        }
    }

    @EnableKafka
    @Configuration
    public static class Config {

        @Bean
        public KafkaFactory kafkaFactory() {
            return new KafkaFactory();
        }

        @Bean
        public KafkaTemplate<Object, Object> kafkaTemplate() {
            return kafkaFactory().createJsonKafkaTemplate();
        }
    }
}
