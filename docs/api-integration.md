# Zero-API-fee integrations

MarketMind deliberately uses **no paid APIs and no API keys**.

## Local LLM (Ollama)

- API: `http://localhost:11434/api/chat`
- Model: `qwen3:4b` (configured in `src/main/resources/config.properties`)
- Install [Ollama](https://ollama.com/), then run `ollama pull qwen3:4b`.
- Run `ollama serve` if Ollama is not already running in the background.
- Check with `curl http://localhost:11434/api/tags`.
- `StrategyAgent` sends only the provided request and gathered evidence to the **local** Ollama service.
- If local Ollama isn't available, the app uses a clearly labelled rule-based fallback.

## Local search (SearXNG)

- API: `http://localhost:8888/search?q=...&format=json`
- Start Docker Desktop, then from MarketMind root:

  ```bash
  bash scripts/start-searxng.sh
  ```

- This creates `.local/searxng/settings.yml` with a randomly generated secret, enables JSON responses, and starts a container bound to **127.0.0.1 only**.
- The local configuration directory is excluded from Git.
- Verify: `curl 'http://localhost:8888/search?q=bookstores+morocco&format=json'`
- If you see HTTP 403, JSON format is not enabled; check `.local/searxng/settings.yml`.
- Search engines can rate limit SearXNG, and its search results are only leads, not verified competitors.
- The app falls back to verified local CSV records when search is unavailable.

## Free public economic data (World Bank)

- API: `https://api.worldbank.org/v2`
- No API key or billing.
- Provides country-wide indicators, **not** business-specific sales or profit forecasts.

## Optional public settings

Environment variables are **optional** (not keys):

```bash
export OLLAMA_URL='http://localhost:11434'
export OLLAMA_MODEL='qwen3:4b'
export SEARXNG_URL='http://localhost:8888'
```

Only set these if you need to override defaults. Do not commit credentials for unrelated APIs.

## Running application

```bash
mvn clean test
mvn exec:java
```

No cloud inference charges. Running Ollama needs sufficient RAM, disk space, and compute. SearXNG still sends search requests to external engines; it is not an offline search database. The first model download and market searches require an internet connection.
