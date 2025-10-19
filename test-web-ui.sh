#!/bin/bash

# Kafka Training Web UI Verification Script
# This script tests all endpoints to verify the web UI connects to the backend

BASE_URL="http://localhost:8080/api/training"
PASSED=0
FAILED=0

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "========================================"
echo "  Kafka Training Web UI Test Suite"
echo "========================================"
echo ""

# Test function
test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local expected_key="$4"

    echo -n "Testing: $name... "

    if [ "$method" = "GET" ]; then
        response=$(curl -s "$BASE_URL$endpoint")
    else
        response=$(curl -s -X POST "$BASE_URL$endpoint")
    fi

    if echo "$response" | grep -q "\"$expected_key\""; then
        echo -e "${GREEN}PASS${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}FAIL${NC}"
        echo "  Response: $response"
        ((FAILED++))
        return 1
    fi
}

# Health Check
echo -e "${BLUE}=== Health Check ===${NC}"
test_endpoint "Health Endpoint" "GET" "/health" "status"
echo ""

# Day 01: Foundation
echo -e "${BLUE}=== Day 01: Foundation ===${NC}"
test_endpoint "List Topics" "GET" "/day01/topics" "topics"
test_endpoint "Demo" "POST" "/day01/demo" "success"
echo ""

# Day 02: Data Flow
echo -e "${BLUE}=== Day 02: Data Flow ===${NC}"
test_endpoint "Data Flow Demo" "POST" "/day02/demo" "success"
test_endpoint "Get Concepts" "GET" "/day02/concepts" "concepts"
echo ""

# Day 03: Producers
echo -e "${BLUE}=== Day 03: Producers ===${NC}"
test_endpoint "Producer Demo" "POST" "/day03/demo" "success"
test_endpoint "Send Message" "POST" "/day03/send-message?topic=user-events&key=test&message=hello" "success"
echo ""

# Day 04: Consumers
echo -e "${BLUE}=== Day 04: Consumers ===${NC}"
test_endpoint "Consumer Demo" "POST" "/day04/demo" "success"
test_endpoint "Consumer Stats" "GET" "/day04/stats" "processedMessages"
echo ""

# Day 05: Kafka Streams
echo -e "${BLUE}=== Day 05: Kafka Streams ===${NC}"
test_endpoint "Streams Demo" "POST" "/day05/demo" "success"
test_endpoint "Streams Status" "GET" "/day05/streams/status" "streams"
echo ""

# Day 06: Schema Management
echo -e "${BLUE}=== Day 06: Schema Management ===${NC}"
test_endpoint "Schema Demo" "POST" "/day06/demo" "success"
test_endpoint "List Schemas" "GET" "/day06/schemas/list" "schemas"
echo ""

# Day 07: Kafka Connect
echo -e "${BLUE}=== Day 07: Kafka Connect ===${NC}"
test_endpoint "Connect Demo" "POST" "/day07/demo" "success"
test_endpoint "Connect Info" "GET" "/day07/connect/info" "version"
test_endpoint "List Connectors" "GET" "/day07/connectors/list" "connectors"
echo ""

# Day 08: Advanced Topics
echo -e "${BLUE}=== Day 08: Advanced Topics ===${NC}"
test_endpoint "Advanced Demo" "POST" "/day08/demo" "success"
test_endpoint "System Status" "GET" "/day08/status" "status"
echo ""

# EventMart
echo -e "${BLUE}=== EventMart Progressive Project ===${NC}"
test_endpoint "EventMart Demo" "POST" "/eventmart/demo" "success"
test_endpoint "EventMart Status" "GET" "/eventmart/status" "status"
echo ""

# Summary
echo "========================================"
echo -e "  Test Results Summary"
echo "========================================"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! Web UI is properly connected to backend.${NC}"
    echo ""
    echo "You can now open http://localhost:8080 in your browser"
    echo "and interact with all 8 training days + EventMart dashboard."
    exit 0
else
    echo -e "${RED}Some tests failed. Check the output above for details.${NC}"
    exit 1
fi
