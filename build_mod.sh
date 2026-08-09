#!/usr/bin/env bash
set -e
if command -v gradle >/dev/null 2>&1; then
  gradle clean build --stacktrace
else
  echo "Gradle is not installed in this environment."
  echo "Push the project to GitHub: .github/workflows/main.yml builds it automatically."
  exit 1
fi
