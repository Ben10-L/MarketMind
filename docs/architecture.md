# JADE Architecture — key-free APIs

```text
Swing MainWindow
    | request: business idea, country, optional budget
MarketMindController
    | UiGatewayAgent
MarketManagerAgent (one Job per UUID)
    |--- MarketResearchAgent -> World Bank Indicators API (free, no key)
    |--- CompetitorAgent ----> local SearXNG search / verified CSV
    |--- RiskAgent ----------> evidence-bound risk checklist
    |--- FinancialAgent -----> illustrative allocation (NOT profit prediction)
    |<-- four AgentFinding replies
    |--- StrategyAgent ------> local Ollama Qwen3 / rule-based fallback
    |--- ReportAgent --------> Markdown report
    |<-- AnalysisResult
UiGatewayAgent -> Swing report view
```

- JADE routes messages; LLM processing happens through the local Ollama HTTP API.
- Local SearXNG aggregates external engine results and can be rate-limited; candidates require manual verification.
- World Bank macroeconomic indicators do not establish demand, profitability, or viability for a specific company.
- No paid API keys are required.
- Report output is always preliminary, and AI claims should be checked against cited sources.
