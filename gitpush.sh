#!/bin/bash

# Check if message is provided
if [ -z "$1" ]; then
  echo "❌ Error: Please provide a commit message."
  echo "Usage: ./gitpush.sh \"Your commit message here\""
  exit 1
fi

# Git operations
echo "📦 Staging files..."
git add .

echo "📝 Committing..."
git commit -m "$1"

echo "🔄 Pulling latest changes..."
# Make sure 'main' is the correct branch name for your repository
git pull origin main --rebase # Or 'master' if that's your branch

echo "🚀 Pushing to GitHub..."
# Make sure 'main' is the correct branch name for your repository
git push origin main # Or 'master' if that's your branch

echo "✅ Done!"
