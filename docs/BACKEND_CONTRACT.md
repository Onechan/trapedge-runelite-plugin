# TrapEdge RuneLite Plugin Backend Contract v1

_Date: 2026-04-05_

## Purpose

This document defines the minimum backend contract for the first TrapEdge RuneLite plugin.

The plugin should stay thin.
The backend should carry:
- triage logic
- item detail logic
- proof-pack reuse
- memory summary

---

# Local dev server

Run locally from the TrapEdge repo:

```bash
npm run triage:live
npm run proof-pack:live
npm run replay:eval
npm run plugin:api
```

Default base URL:
- `http://127.0.0.1:4311`

---

# Endpoints

## `GET /health`
Returns a basic heartbeat.

## `GET /api/plugin/bootstrap`
Returns the plugin panel's main dataset.

### Response shape
- `generatedAt`
- `sourceGeneratedAt`
- `triage[]`
- `actions[]`
- `memoryPressure[]`
- `proofCases[]`
- `replaySummary`

## `GET /api/plugin/item/:itemId`
Returns the selected item's detailed judgment.

### Response shape
- item identity
- confidence / action labels
- case category
- reason summary
- `flagDetails[]`
- `strengths[]`
- `memoryNotes[]`
- `matchedRules[]`
- `raw`
- `proofCase`
- `liveAssessment`

## `GET /api/plugin/tester-pack`
Returns proof-pack payload for demo / tester sessions.

## Plugin Hub prep truth
The current TrapEdge Plugin Hub prep pass treats the plugin client as read-only.

That means the Plugin Hub-ready client path relies on:
- `GET /api/plugin/bootstrap`
- `GET /api/plugin/item/:itemId`
- optional `GET /api/plugin/tester-pack`

Any older writeback/postmortem endpoint should be treated as private-lab history, not part of the current reviewer-safe client posture.

---

# Design rule

The plugin should not reproduce the whole judgment engine locally in v1.
It should render backend-fed judgments and keep the client logic thin.

---

# One-line conclusion

> RuneLite v1 should be a thin panel over TrapEdge's judgment engine, not a second independently-maintained logic stack.
