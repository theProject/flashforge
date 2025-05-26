#!/bin/bash

echo "🔄 Pulling latest changes from GitHub..."

# Pull with rebase to keep history clean
# Make sure 'main' is the correct branch name for your repository
git pull origin main --rebase # Or 'master' if that's your branch

if [ $? -eq 0 ]; then
  echo "✅ Successfully pulled latest updates!"
else
  echo "❌ Something went wrong. Please check for conflicts."
fi
