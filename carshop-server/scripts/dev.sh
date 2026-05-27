#!/usr/bin/env bash
# 启动开发服务器
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ -d .venv ]]; then
  source .venv/bin/activate
fi

exec uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
