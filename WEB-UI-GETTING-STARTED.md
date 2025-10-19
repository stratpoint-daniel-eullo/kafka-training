# Getting Started with the Kafka Training Web UI

## Quick Start

### 1. Access the Web Interface
Open your browser and go to:
```
http://localhost:8080
```

### 2. First Steps - Test the Basics (5 minutes)

#### Step 1: Create Your First Topic
1. Find the **Day 1: Kafka Fundamentals** section
2. In the input fields, enter:
   - Topic Name: `my-first-topic`
   - Partitions: `3`
3. Click **Create Topic** button
4. You should see a success message below

#### Step 2: Send Your First Message
1. Find the **Day 3: Message Producers** section
2. In the input fields, enter:
   - Topic: `my-first-topic`
   - Key: `user-1`
   - Message: `Hello Kafka!`
3. Click **Send** button
4. You should see a success message

#### Step 3: Consume Your Message
1. Find the **Day 4: Message Consumers** section
2. In the input fields, enter:
   - Topic: `my-first-topic`
   - Group ID: `my-consumer-group`
   - Max Messages: `5`
3. Click **Consume Messages** button
4. Check the application logs to see your message being consumed

## Common Workflows

### Workflow 1: Basic Producer-Consumer Pattern
```
1. Create a topic (Day 1)
2. Send messages (Day 3)
3. Consume messages (Day 4)
4. Check consumer lag (Day 2)
```

### Workflow 2: Try the EventMart Demo
```
1. Scroll to "EventMart Progressive Project" section
2. Click "Create Topics" - sets up all EventMart topics
3. Click "Run Full Demo" - runs complete e-commerce simulation
4. Watch the logs for user registrations, product creation, orders
```

### Workflow 3: Kafka Streams Processing
```
1. Go to "Day 5: Kafka Streams" section
2. Click "Run Demo" - generates test data
3. Click "Start" on "User Activity" stream
4. Click "Get All Status" to see it running
5. Check logs for real-time processing
6. Click "Stop" when done
```

### Workflow 4: Schema Registry with Avro
```
1. Go to "Day 6: Schema Management"
2. Click "Run Demo" - initializes Schema Registry
3. Fill in user details:
   - User ID: user-123
   - Name: John Doe
   - Email: john@example.com
4. Click "Produce Avro" - sends message with schema
5. Click "Consume Avro" - reads the Avro message
6. Click "List Schemas" - see registered schemas
```

## Understanding the UI Layout

### Each Module Has:
- **Run Demo** button - Automated demonstration of that day's concepts
- **Action buttons** - Specific operations you can perform
- **Input fields** - Customize parameters (optional)
- **Result area** - Shows success/error messages and data

### Status Indicators:
- **Green dot** (top right) = System Ready
- **Stream indicators** (Day 5) = Shows if streams are running

## Step-by-Step Learning Path

### Beginner (Start Here)
1. **Day 1**: Learn Kafka basics
   - Click "Run Demo"
   - Try creating a topic
   - Click "List Topics" to see all topics

2. **Day 3**: Send messages
   - Click "Run Demo"
   - Send a custom message
   - Try "Send Batch" to send 10 messages

3. **Day 4**: Receive messages
   - Click "Run Demo"
   - Consume messages from the topic you created
   - Click "Get Stats" to see consumption statistics

### Intermediate
4. **Day 2**: Understand data flow
   - Click "Run Demo"
   - Check "Consumer Lag" to see offset tracking
   - View "Concepts" to understand partitioning

5. **Day 5**: Stream processing
   - Click "Run Demo"
   - Start the "User Activity" stream
   - Watch real-time processing in logs

### Advanced
6. **Day 6**: Schema management
   - Click "Run Demo"
   - Produce and consume Avro messages
   - Check schema compatibility

7. **Day 7**: Kafka Connect
   - Click "Run Demo"
   - View connector info
   - Create source/sink connectors

8. **Day 8**: Production features
   - Click "Run Demo"
   - View cluster metrics
   - Check security configuration

## Tips

### Where to See Results
- **Success/Error messages** appear below each module (green/red boxes)
- **Detailed logs** are in your terminal where you ran `mvn spring-boot:run`
- **Real-time data** flows through the topics you create

### Best Practices
1. **Start with Run Demo** - Each section's demo button shows you how things work
2. **Check the logs** - Terminal output shows what's happening behind the scenes
3. **Small tests first** - Send a few messages before sending batches
4. **Use default values** - Pre-filled values are good starting points

### Troubleshooting
- **No response?** - Check if Kafka is running (`docker-compose ps`)
- **Errors in UI?** - Look at terminal logs for detailed error messages
- **Topics not found?** - Create them first in Day 1
- **Can't consume?** - Make sure you've sent messages first

## Example: Complete Test Flow

Here's a complete workflow to test everything works:

```
Step 1: Create Topic
- Go to Day 1
- Topic Name: test-topic
- Partitions: 3
- Click "Create Topic"
- ✓ Should see success message

Step 2: Send Messages
- Go to Day 3
- Topic: test-topic
- Key: user-1
- Message: Test message
- Click "Send"
- ✓ Should see success message

Step 3: Send Batch
- Still in Day 3
- Click "Send Batch"
- ✓ Should see "10 messages sent"

Step 4: Consume Messages
- Go to Day 4
- Topic: test-topic
- Group ID: test-group
- Max Messages: 5
- Click "Consume Messages"
- ✓ Check terminal logs to see consumed messages

Step 5: Check Lag
- Go to Day 2
- Group ID: test-group
- Click "Check Consumer Lag"
- ✓ Should show lag information
```

## Quick Reference

### Most Useful Buttons for Beginners
| Button | Where | What It Does |
|--------|-------|--------------|
| Run Demo | Any Day | Shows automated demonstration |
| Create Topic | Day 1 | Creates a new Kafka topic |
| Send | Day 3 | Sends one message |
| Send Batch | Day 3 | Sends 10 messages |
| Consume Messages | Day 4 | Reads messages from topic |
| List Topics | Day 1 | Shows all topics |

### Default Topics Created by Demos
- `user-events` - User activity data
- `order-events` - Order transactions
- `demo-topic-1`, `demo-topic-2` - Test topics
- `eventmart-*` - E-commerce platform topics

## Next Steps

Once you're comfortable with the basics:
1. Try the **EventMart demo** - Complete e-commerce simulation
2. Experiment with **Kafka Streams** - Real-time processing
3. Test **Schema Registry** - Avro serialization
4. Explore **monitoring** - Day 8 metrics

## Getting Help

- **API Documentation**: http://localhost:8080/api/training/modules
- **Health Check**: http://localhost:8080/actuator/health
- **Kafka Documentation**: https://kafka.apache.org/documentation/
