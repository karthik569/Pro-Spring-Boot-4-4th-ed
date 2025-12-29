# Chapter 16: Spring AI - Building an AI-Powered CRM Assistant

This chapter demonstrates how to build an AI-powered CRM Assistant using Spring AI, integrating with Large Language Models (LLMs), vector databases, and implementing advanced AI patterns like RAG (Retrieval-Augmented Generation) and function calling.

## Projects Overview

### 1. **management** - Main CRM Service with AI Assistant
Enhanced version of the management service from Chapter 15, now with AI capabilities:
- **RAG (Retrieval-Augmented Generation)**: Query product knowledge using vector similarity search
- **Function Calling**: AI can call functions to perform customer operations
- **Database Agent**: Natural language to SQL query conversion
- **Chat Memory**: Conversational context retention

### 2. **mcp-currency-server** - External Tool Server
Simple currency conversion service demonstrating external tool integration:
- Currency conversion API
- Exchange rate queries
- Support for 10 major currencies (USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, MXN)

### 3. **crm-assistant-shell** - CLI Client
Spring Shell-based command-line interface for interacting with the AI assistant:
- Interactive chat commands
- Product knowledge queries
- Database queries
- Function calling demonstrations

## Key Technologies

- **Spring AI 1.0.0-M5**: Framework for building AI-powered applications
- **Spring Boot 4.0.1**: Application framework
- **Ollama**: Local LLM runtime (Llama 3.2, nomic-embed-text)
- **OpenAI GPT-4**: Cloud-based LLM option
- **pgvector**: PostgreSQL extension for vector similarity search
- **Spring Shell 3.3.4**: Interactive CLI framework

## Prerequisites

### 1. Install Ollama and Download Models

```bash
# Install Ollama (macOS)
brew install ollama

# Start Ollama server
ollama serve

# In another terminal, download models
ollama pull llama3.2          # Chat model
ollama pull nomic-embed-text  # Embedding model
```

### 2. Setup PostgreSQL with pgvector

The management service requires PostgreSQL with the pgvector extension:

```bash
# Using Docker (recommended)
docker run -d \
  --name postgres-management \
  -e POSTGRES_DB=managementdb \
  -e POSTGRES_USER=management \
  -e POSTGRES_PASSWORD=password \
  -p 5433:5432 \
  pgvector/pgvector:pg17
```

## Running the Applications

### Start Management Service with AI

```bash
cd management
./gradlew bootRun
# Runs on port 8082
# Will initialize product documents in vector store on startup
```

### Start Currency Server (Optional)

```bash
cd mcp-currency-server
./gradlew bootRun
# Runs on port 9090
```

### Start Shell Client

```bash
cd crm-assistant-shell
./gradlew bootRun
# Opens interactive shell
```

## Using the AI Assistant

### Via Shell Client

#### Basic Chat
```shell
shell:>chat -m "What is a CRM system?"
```

#### Product Knowledge (RAG)
```shell
shell:>products -m "What features are included in the Professional plan?"
```

#### Database Queries
```shell
shell:>query -m "Show me all customers"
```

#### Function Calling
```shell
shell:>functions -m "Find customer with email john@example.com"
```

### Via REST API

```bash
# Basic chat
curl -X POST http://localhost:8082/api/assistant/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4=" \
  -d '{"message": "What is CRM?"}'
```

## Key Components

- **RAGService**: Retrieval-Augmented Generation for product knowledge
- **DatabaseAgentService**: Natural language to SQL conversion
- **CustomerTools**: Function calling for customer operations
- **ProductDocumentInitializer**: Loads product docs into vector store

## Configuration

Edit `management/src/main/resources/application.yml` to configure AI models and vector store.

## Compilation

All projects compile successfully:

```bash
# Management
cd management && ./gradlew compileJava compileTestJava

# Currency Server
cd mcp-currency-server && ./gradlew compileJava compileTestJava

# Shell Client
cd crm-assistant-shell && ./gradlew compileJava
```

All builds pass successfully!
