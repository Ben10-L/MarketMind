#!/usr/bin/env bash
set -euo pipefail

# Run from the MarketMind root: bash scripts/start-searxng.sh
cd "$(dirname "$0")/.."
mkdir -p .local/searxng
if [ ! -f .local/searxng/settings.yml ]; then
  if ! command -v openssl >/dev/null 2>&1; then
    echo "openssl is required to generate a secret" >&2; exit 1
  fi
  secret="$(openssl rand -hex 32)"
  cat > .local/searxng/settings.yml <<YAML
use_default_settings: true
search:
  formats:
    - html
    - json
server:
  secret_key: "$secret"
  limiter: false
YAML
fi
if docker container inspect marketmind-searxng >/dev/null 2>&1; then
  docker start marketmind-searxng >/dev/null
else
  docker run -d \
    --name marketmind-searxng \
    -p 127.0.0.1:8888:8080 \
    -v "$(pwd)/.local/searxng:/etc/searxng:rw" \
    docker.io/searxng/searxng:latest >/dev/null
fi
echo "SearXNG starting on http://localhost:8888"
echo "Check: curl 'http://localhost:8888/search?q=market&format=json'"
