package com.training.kafka.cli;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Simple CLI tool to produce messages to Kafka
 * Used by kafka-training-cli.sh script
 *
 * Usage: java ProducerCLI <topic> <key> <value> <bootstrap-servers>
 */
public class ProducerCLI {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: ProducerCLI <topic> <key> <value> <bootstrap-servers>");
            System.exit(1);
        }

        String topic = args[0];
        String key = args[1];
        String value = args[2];
        String bootstrapServers = args[3];

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();

            System.out.println("✓ Message sent successfully:");
            System.out.println("  Topic: " + metadata.topic());
            System.out.println("  Partition: " + metadata.partition());
            System.out.println("  Offset: " + metadata.offset());
            System.out.println("  Key: " + key);
            System.out.println("  Value: " + value);

        } catch (Exception e) {
            System.err.println("❌ Failed to send message: " + e.getMessage());
            System.exit(1);
        }
    }
}
