# MarketMind verification

## Build and automated tests

Verified on macOS with OpenJDK 17. Maven compiled the Java sources and built `target/marketmind-1.0.0.jar`. All six JUnit tests passed, covering request validation, budget allocation, report assembly, and PDF export. The repository has no automated end-to-end JADE or desktop UI tests.

```sh
mvn clean verify
```

## Desktop smoke test

A local test harness called the production `com.marketmind.Main` entry point and operated the visible Swing controls on the Event Dispatch Thread.

1. The JADE main container started and the Swing workspace opened.
2. The harness selected **Evaluate a bookstore**, used country **MA** and currency **MAD**, entered **50000**, and submitted the request.
3. The activity log showed all four dispatches and findings, then strategy and report completion. The generated report contained population, inflation, and GDP data returned by the World Bank.
4. Ollama and SearXNG were pointed to an unused loopback port. The result contained the rule-based strategy and reported that no matching local competitor entries were available.
5. Swing displayed the report preview. Both the Markdown and PDF outputs were present and nonempty.

The PNGs in [docs/screenshots](docs/screenshots/) are direct captures of the running Swing components from this smoke test.

## Remaining desktop checks

- Run the same workflow with a live SearXNG instance and the configured Ollama model.
- Open a generated PDF in the system viewer and verify clipboard integration with **Copy report text**.
- Verify the native window icon and layout on Windows and Linux, including high-DPI displays.
- Exercise concurrent requests, worker failure, and the manager timeout through the desktop interface.
