# ACL Messaging Contracts

| From | To | Ontology | Performative | Serialized payload |
|---|---|---|---|---|
| ui | manager | `marketmind.request` | REQUEST | `MarketRequest` |
| manager | researcher/competitor/risk/financial | `marketmind.work` | REQUEST | `MarketRequest` |
| workers | manager | `marketmind.finding` | INFORM | `AgentFinding` |
| manager | strategy | `marketmind.strategy.request` | REQUEST | `AnalysisBundle` |
| strategy | manager | `marketmind.finding` | INFORM | `AgentFinding` |
| manager | report | `marketmind.report.request` | REQUEST | `AnalysisBundle` |
| report | manager | `marketmind.report.result` | INFORM | `AnalysisResult` |
| manager | ui | `marketmind.ui.result` | INFORM | `AnalysisResult` |
| manager | ui | `marketmind.ui.status` | INFORM | plain text |

Replies preserve the request's conversation ID via JADE `createReply()`.
