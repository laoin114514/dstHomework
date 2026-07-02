#!/bin/bash
# 工具脚本 - 编译、测试、清理等常用操作
# Usage: ./scripts/edge.sh [build|test|clean|package]

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

print_help() {
    echo "用法: ./scripts/edge.sh <command>"
    echo ""
    echo "命令:"
    echo "  build     编译项目"
    echo "  test      运行测试"
    echo "  clean     清理构建产物"
    echo "  package   打包为可执行 JAR"
    echo "  deps      查看依赖树"
    echo "  verify    编译 + 测试"
    echo ""
}

case "${1:-}" in
    build)
        echo ">>> 编译..."
        mvn compile
        ;;
    test)
        echo ">>> 运行测试..."
        mvn test
        ;;
    clean)
        echo ">>> 清理..."
        mvn clean
        rm -rf target/
        ;;
    package)
        echo ">>> 打包..."
        mvn package -DskipTests
        ;;
    deps)
        echo ">>> 依赖树..."
        mvn dependency:tree
        ;;
    verify)
        echo ">>> 编译 + 测试..."
        mvn verify
        ;;
    *)
        print_help
        ;;
esac
