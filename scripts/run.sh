#!/bin/bash
# 启动中国地图着色应用
# Usage: ./scripts/run.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo ">>> 编译项目..."
mvn compile -q

echo ">>> 启动应用..."
mvn javafx:run
