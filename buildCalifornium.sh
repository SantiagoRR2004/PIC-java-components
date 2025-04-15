#!/bin/bash

# Set the repository URL and directory
REPO_URL="https://github.com/eclipse/californium.tools.git"
DIR_NAME="californium.tools"

# Check if the directory exists
if [ -d "$DIR_NAME" ]; then
    echo "Directory '$DIR_NAME' exists. Checking for updates..."
    # Move into the directory
    cd "$DIR_NAME" || { echo "Failed to enter directory"; exit 1; }
    # Check if there are any changes to pull
    git remote update
    LOCAL=$(git rev-parse @)
    REMOTE=$(git rev-parse @{u})
    BASE=$(git merge-base @ @{u})

    if [ "$LOCAL" = "$REMOTE" ]; then
        echo "Repository is up-to-date. No action required."
    elif [ "$LOCAL" = "$BASE" ]; then
        echo "Local repository is behind. Updating..."
        git pull
    else
        echo "Repository is ahead of remote or diverged. Please resolve manually."
    fi
else
    # Clone the repository
    echo "Directory '$DIR_NAME' does not exist. Cloning repository..."
    git clone "$REPO_URL"
    # Move into the directory
    cd "$DIR_NAME" || { echo "Failed to enter directory"; exit 1; }
fi

# Build the project with Maven
echo "Building the project with Maven..."
mvn clean install
