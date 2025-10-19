#!/bin/bash

# Kafka Training CLI Wrapper Script
# This script provides a simple CLI interface for running pure Kafka examples
# without needing to type long Java commands

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JAR_FILE="target/kafka-training-java-1.0.0.jar"
BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"

# Banner
print_banner() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════╗"
    echo "║         Kafka Training CLI - Data Engineers          ║"
    echo "║              Pure Kafka • No Frameworks               ║"
    echo "╚═══════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

OPTIONS:
    --day <1-8>              Specify training day
    --demo <name>            Run demonstration (foundation, producer, consumer, etc.)
    create-topic             Create a Kafka topic
    produce                  Send messages to Kafka
    consume                  Read messages from Kafka
    help                     Show this help message

EXAMPLES:
    # Run Day 1 foundation demo
    $0 --day 1 --demo foundation

    # Run Day 3 producer demo
    $0 --day 3 --demo producer

    # Create a topic
    $0 create-topic --name user-events --partitions 3 --replication-factor 1

    # Produce a message
    $0 produce --topic user-events --key user-123 --value "Hello Kafka"

    # Consume messages
    $0 consume --topic user-events --group my-consumer-group

ENVIRONMENT VARIABLES:
    KAFKA_BOOTSTRAP_SERVERS  Kafka bootstrap servers (default: localhost:9092)

EOF
}

# Check if JAR exists
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        echo -e "${RED}ERROR: JAR file not found at $JAR_FILE${NC}"
        echo -e "${YELLOW}Please build the project first: mvn clean package${NC}"
        exit 1
    fi
}

# Run Day demo
run_day_demo() {
    local day=$1
    local demo=$2

    case $day in
        1)
            case $demo in
                foundation)
                    echo -e "${GREEN}Running Day 1: Foundation Demo${NC}"
                    java -cp "$JAR_FILE" com.training.kafka.cli.Day01FoundationCLI
                    ;;
                *)
                    echo -e "${RED}Unknown demo for Day 1: $demo${NC}"
                    echo -e "${YELLOW}Available demos: foundation${NC}"
                    exit 1
                    ;;
            esac
            ;;
        3)
            case $demo in
                producer)
                    echo -e "${GREEN}Running Day 3: Producer Demo${NC}"
                    java -cp "$JAR_FILE" com.training.kafka.Day03Producers.AdvancedProducer
                    ;;
                simple)
                    echo -e "${GREEN}Running Day 3: Simple Producer${NC}"
                    java -cp "$JAR_FILE" com.training.kafka.Day03Producers.SimpleProducer
                    ;;
                *)
                    echo -e "${RED}Unknown demo for Day 3: $demo${NC}"
                    echo -e "${YELLOW}Available demos: producer, simple${NC}"
                    exit 1
                    ;;
            esac
            ;;
        4)
            case $demo in
                consumer)
                    echo -e "${GREEN}Running Day 4: Consumer Demo${NC}"
                    java -cp "$JAR_FILE" com.training.kafka.cli.Day04ConsumerCLI
                    ;;
                *)
                    echo -e "${RED}Unknown demo for Day 4: $demo${NC}"
                    echo -e "${YELLOW}Available demos: consumer${NC}"
                    exit 1
                    ;;
            esac
            ;;
        *)
            echo -e "${RED}Day $day not yet implemented in CLI${NC}"
            echo -e "${YELLOW}Available days: 1, 3, 4${NC}"
            exit 1
            ;;
    esac
}

# Create topic
create_topic() {
    local topic_name=""
    local partitions=3
    local replication_factor=1

    while [[ $# -gt 0 ]]; do
        case $1 in
            --name)
                topic_name="$2"
                shift 2
                ;;
            --partitions)
                partitions="$2"
                shift 2
                ;;
            --replication-factor)
                replication_factor="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done

    if [ -z "$topic_name" ]; then
        echo -e "${RED}ERROR: Topic name is required${NC}"
        echo "Usage: $0 create-topic --name TOPIC_NAME [--partitions NUM] [--replication-factor NUM]"
        exit 1
    fi

    echo -e "${GREEN}Creating topic: $topic_name${NC}"
    echo "  Partitions: $partitions"
    echo "  Replication Factor: $replication_factor"

    java -cp "$JAR_FILE" \
        com.training.kafka.cli.TopicCreatorCLI \
        "$topic_name" \
        "$partitions" \
        "$replication_factor" \
        "$BOOTSTRAP_SERVERS"
}

# Produce message
produce_message() {
    local topic=""
    local key=""
    local value=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            --topic)
                topic="$2"
                shift 2
                ;;
            --key)
                key="$2"
                shift 2
                ;;
            --value)
                value="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done

    if [ -z "$topic" ] || [ -z "$value" ]; then
        echo -e "${RED}ERROR: Topic and value are required${NC}"
        echo "Usage: $0 produce --topic TOPIC_NAME --key KEY --value VALUE"
        exit 1
    fi

    echo -e "${GREEN}Producing message to topic: $topic${NC}"
    echo "  Key: $key"
    echo "  Value: $value"

    java -cp "$JAR_FILE" \
        com.training.kafka.cli.ProducerCLI \
        "$topic" \
        "$key" \
        "$value" \
        "$BOOTSTRAP_SERVERS"
}

# Consume messages
consume_messages() {
    local topic=""
    local group="cli-consumer-group"

    while [[ $# -gt 0 ]]; do
        case $1 in
            --topic)
                topic="$2"
                shift 2
                ;;
            --group)
                group="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                exit 1
                ;;
        esac
    done

    if [ -z "$topic" ]; then
        echo -e "${RED}ERROR: Topic is required${NC}"
        echo "Usage: $0 consume --topic TOPIC_NAME [--group GROUP_ID]"
        exit 1
    fi

    echo -e "${GREEN}Consuming messages from topic: $topic${NC}"
    echo "  Consumer Group: $group"
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    echo ""

    java -cp "$JAR_FILE" \
        com.training.kafka.cli.ConsumerCLI \
        "$topic" \
        "$group" \
        "$BOOTSTRAP_SERVERS"
}

# Main
main() {
    print_banner

    if [ $# -eq 0 ]; then
        usage
        exit 0
    fi

    check_jar

    case $1 in
        --day)
            if [ -z "$2" ] || [ -z "$3" ] || [ "$3" != "--demo" ] || [ -z "$4" ]; then
                echo -e "${RED}ERROR: Invalid day/demo syntax${NC}"
                echo "Usage: $0 --day <1-8> --demo <demo-name>"
                exit 1
            fi
            run_day_demo "$2" "$4"
            ;;
        create-topic)
            shift
            create_topic "$@"
            ;;
        produce)
            shift
            produce_message "$@"
            ;;
        consume)
            shift
            consume_messages "$@"
            ;;
        help|--help|-h)
            usage
            ;;
        *)
            echo -e "${RED}Unknown command: $1${NC}"
            usage
            exit 1
            ;;
    esac
}

main "$@"
