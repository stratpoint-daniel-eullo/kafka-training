package com.training.kafka.cli;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Simple CLI tool to consume messages from Kafka
 * Used by kafka-training-cli.sh script
 *
 * Usage: java ConsumerCLI <topic> <group-id> <bootstrap-servers>
 */
public class ConsumerCLI {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: ConsumerCLI <topic> <group-id> <bootstrap-servers>");
            System.exit(1);
        }

        String topic = args[0];
        String groupId = args[1];
        String bootstrapServers = args[2];

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            System.out.println("Consuming from topic: " + topic);
            System.out.println("Consumer group: " + groupId);
            System.out.println("Press Ctrl+C to stop\n");

            int messageCount = 0;

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    System.out.println("Message #" + messageCount);
                    System.out.println("  Partition: " + record.partition());
                    System.out.println("  Offset: " + record.offset());
                    System.out.println("  Key: " + record.key());
                    System.out.println("  Value: " + record.value());
                    System.out.println();
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
