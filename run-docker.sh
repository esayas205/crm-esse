#!/bin/bash

# Function to display usage
usage() {
    echo "Usage: $0 [mvn-command]"
    echo "Example: $0 test"
    echo "         $0 clean install"
    echo "If no command is provided, it defaults to 'mvn test'"
    exit 1
}

# Determine the command to run
COMMAND=${*:-test}

echo "Running: mvn $COMMAND through docker-compose.test.yml"

# Run the command using docker-compose
docker-compose -f docker-compose.test.yml run --rm test-runner mvn $COMMAND
