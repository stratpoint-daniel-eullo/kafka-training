# Quick Reference - Which File Do I Need?

## 🎯 **I Want To...**

### Learn Pure Kafka (Data Engineers)
→ **[README-DATA-ENGINEERS.md](./README-DATA-ENGINEERS.md)**

### Learn Spring Boot + Kafka (Java Developers)
→ **[README.md](./README.md)**

### Decide Which Path to Take
→ **[LEARNING-PATHS-UPDATED.md](./LEARNING-PATHS-UPDATED.md)**

### Understand What Changed
→ **[REFACTOR-SUMMARY.md](./REFACTOR-SUMMARY.md)**

### Get Started in 60 Seconds
→ **[START-HERE.md](./START-HERE.md)**

---

## 📁 **File Guide by Role**

### **Data Engineers**

| File | Purpose |
|------|---------|
| `README-DATA-ENGINEERS.md` | Your main guide - START HERE |
| `bin/kafka-training-cli.sh` | CLI tool for running examples |
| `examples/python/day03_producer.py` | Python producer example |
| `examples/python/day04_consumer.py` | Python consumer example |
| `src/.../cli/Day01FoundationCLI.java` | Java AdminClient example |
| `src/.../cli/Day04ConsumerCLI.java` | Java consumer example |
| `src/.../Day03Producers/AdvancedProducer.java` | Java producer (pure Kafka) |

### **Java Developers**

| File | Purpose |
|------|---------|
| `README.md` | Your main guide - START HERE |
| `WEB-UI-GETTING-STARTED.md` | Web UI walkthrough |
| `src/.../services/Day03ProducerService.java` | Spring producer service |
| `src/.../services/Day04ConsumerService.java` | Spring consumer service |
| `src/.../controllers/TrainingController.java` | REST API endpoints |

---

## ⚡ **Quick Commands**

### Data Engineers (CLI)

```bash
# Build project
mvn clean package

# Run Day 1 demo
./bin/kafka-training-cli.sh --day 1 --demo foundation

# Run Day 3 producer
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.Day03Producers.AdvancedProducer

# Run Python producer
python examples/python/day03_producer.py
```

### Java Developers (Web UI)

```bash
# Start Spring Boot app
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Open browser
open http://localhost:8080

# Use REST API
curl -X POST http://localhost:8080/api/training/day03/demo
```

---

## 🗺️ **Directory Map**

```
kafka-training-java/
│
├─ 📖 README.md                    [Java Developers]
├─ 📖 README-DATA-ENGINEERS.md      [Data Engineers]
├─ 📖 LEARNING-PATHS-UPDATED.md     [Path Decision Guide]
├─ 📖 START-HERE.md                 [Quick Start]
│
├─ bin/
│  └─ kafka-training-cli.sh         [CLI tool]
│
├─ src/main/java/.../
│  ├─ cli/                          [Pure Kafka - Data Engineers]
│  ├─ Day03Producers/               [Pure Kafka - Both paths]
│  ├─ services/                     [Spring Boot - Java Developers]
│  └─ controllers/                  [Spring Boot - Java Developers]
│
├─ examples/python/                 [Python - Data Engineers]
│  ├─ day03_producer.py
│  └─ day04_consumer.py
│
├─ documentations/                  [Concepts - Both paths]
│  ├─ day01-foundation.md
│  └─ ...
│
└─ docs/                            [MkDocs site - Both paths]
   └─ index.md
```

---

## 💡 **Common Questions**

### Q: Which README should I read?

**Data Engineer?** → `README-DATA-ENGINEERS.md`
**Java Developer?** → `README.md`
**Not sure?** → `LEARNING-PATHS-UPDATED.md`

### Q: Where are the pure Kafka examples?

- **Java**: `src/main/java/com/training/kafka/cli/`
- **Java (Day 3)**: `src/main/java/com/training/kafka/Day03Producers/`
- **Python**: `examples/python/`

### Q: Where are the Spring Boot examples?

- **Services**: `src/main/java/com/training/kafka/services/`
- **Controllers**: `src/main/java/com/training/kafka/controllers/`
- **Web UI**: `src/main/resources/static/`

### Q: How do I run the CLI examples?

```bash
# Option 1: Use the CLI script
./bin/kafka-training-cli.sh --day 1 --demo foundation

# Option 2: Run Java directly
java -cp target/kafka-training-java-1.0.0.jar \
  com.training.kafka.cli.Day01FoundationCLI
```

### Q: How do I run the Spring Boot app?

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Then open: http://localhost:8080
```

### Q: Can I use both paths?

**Yes!** They share the same Kafka cluster. You can run CLI examples and Spring Boot simultaneously.

---

## 🎓 **Learning Path Shortcuts**

### **I'm a complete beginner**
1. Read: `LEARNING-PATHS-UPDATED.md`
2. Choose your path based on role
3. Follow that path's README

### **I know Python, want to learn Kafka**
1. Read: `README-DATA-ENGINEERS.md`
2. Try: `examples/python/day03_producer.py`

### **I know Java, want Spring Boot + Kafka**
1. Read: `README.md`
2. Run: `mvn spring-boot:run`
3. Open: `http://localhost:8080`

### **I know Kafka, want pure APIs**
1. Read: `README-DATA-ENGINEERS.md`
2. Run: `./bin/kafka-training-cli.sh`

---

**Still confused?** Read [LEARNING-PATHS-UPDATED.md](./LEARNING-PATHS-UPDATED.md) for a complete decision tree.
