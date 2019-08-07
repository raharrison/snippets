public class SendToDeadLetterQueueExceptionHandler implements DeserializationExceptionHandler {
    KafkaProducer<byte[], byte[]> dlqProducer;
    String dlqTopic;
 
    @Override
    public DeserializationHandlerResponse handle(final ProcessorContext context,
                                                 final ConsumerRecord<byte[], byte[]> record,
                                                 final Exception exception) {
 
        log.warn("Exception caught during Deserialization, sending to the dead queue topic; " +
            "taskId: {}, topic: {}, partition: {}, offset: {}",
            context.taskId(), record.topic(), record.partition(), record.offset(),
            exception);
 
        dlqProducer.send(new ProducerRecord<>(dlqTopic, record.timestamp(), record.key(), record.value(), record.headers())).get();
 
        return DeserializationHandlerResponse.CONTINUE;
    }
 
    @Override
    public void configure(final Map<String, ?> configs) {
        dlqProducer = .. // get a producer from the configs map
        dlqTopic = .. // get the topic name from the configs map
    }
}