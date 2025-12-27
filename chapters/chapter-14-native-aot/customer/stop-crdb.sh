#!/bin/bash
pkill -f "cockroach"

# Optional: Clean up data directories
# rm -rf node1 node2 node3

echo "CockroachDB cluster stopped and data cleaned."
