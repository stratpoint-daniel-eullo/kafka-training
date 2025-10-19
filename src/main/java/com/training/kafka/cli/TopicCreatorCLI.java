package com.training.kafka.cli;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;

/**
 * Simple CLI tool to create Kafka topics
 * Used by kafka-training-cli.sh script
 *
 * Usage: java TopicCreatorCLI <topic-name> <partitions> <replication-factor> <bootstrap-servers>
 */
public class TopicCreatorCLI {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: TopicCreatorCLI <topic-name> <partitions> <replication-factor> <bootstrap-servers>");
            System.exit(1);
        }

        String topicName = args[0];
        int partitions = Integer.parseInt(args[1]);
        short replicationFactor = Short.parseShort(args[2]);
        String bootstrapServers = args[3];

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient admin = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
            result.all().get();
            System.out.println("✓ Topic created successfully: " + topicName);
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("⚠ Topic already exists: " + topicName);
            } else {
                System.err.println("❌ Failed to create topic: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}
