#!/bin/bash

# Ensure cockroach is in the path
if ! [ -x "$(command -v cockroach)" ]; then
  echo 'Error: cockroach is not installed.' >&2
  exit 1
fi

# Start node 1
cockroach start --insecure --store=node1 --listen-addr=localhost:26257 --http-addr=localhost:8081 --join=localhost:26257,localhost:26258,localhost:26259 --locality=region=us-east-1 --background

# Start node 2
cockroach start --insecure --store=node2 --listen-addr=localhost:26258 --http-addr=localhost:8082 --join=localhost:26257,localhost:26258,localhost:26259 --locality=region=us-west-1 --background

# Start node 3
cockroach start --insecure --store=node3 --listen-addr=localhost:26259 --http-addr=localhost:8083 --join=localhost:26257,localhost:26258,localhost:26259 --locality=region=eu-central-1 --background

# Initialize the cluster
cockroach init --insecure --host=localhost:26257

echo "CockroachDB cluster started."
echo "UI available at http://localhost:8081"
echo "You can connect using: cockroach sql --insecure"
echo "NOTE: Remember to create the customer_db and management_db databases."
echo "SQL:"
echo "create database customer_db;"
echo "create database management_db;"
