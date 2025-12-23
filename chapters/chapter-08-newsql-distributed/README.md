# Chapter 8: NewSQL - Distributed SQL Transactions with Spring Boot

This directory contains the source code for Chapter 8 of the book "Pro Spring Boot 4".

## Learning Objectives
- Understand distributed SQL databases.
- Work with CockroachDB.
- Implement distributed transactions.
- Handle geo-distributed data.

## Key Topics
- The challenge of distributed systems (resilience, scalability, latency)
- Setting up a local multi-region CockroachDB cluster
- Mandatory SERIALIZABLE isolation and its implications
- Handling transaction contention with Spring Retry
- Multi-region optimization using Spring Cache and Redis
- Change Data Capture (CDC) and Webhooks
- Modern concurrency with Virtual Threads
- Testing distributed transactions and retry logic
- Comparing SQL, NoSQL, and NewSQL databases
