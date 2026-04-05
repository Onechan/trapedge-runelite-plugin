# TrapEdge RuneLite Plugin Reviewer Note V1

_Date: 2026-04-05_

## One-sentence description

TrapEdge is a RuneLite sidebar plugin that shows trap risk, next action, and execution memory for watched flips.

---

## What the plugin does

The plugin:
- loads a snapshot of watched items
- shows a compact triage view
- lets the user inspect one selected item at a time
- surfaces replay / memory context that helps explain the judgment

It is intended as decision support.

---

## What data is fetched

Current plugin API calls:
- `GET /api/plugin/bootstrap`
- `GET /api/plugin/item/:itemId`

Current Plugin Hub prep posture:
- read-oriented client
- no postmortem writeback from the plugin client

---

## What the plugin does **not** do

The plugin does not:
- automate Grand Exchange actions
- click, buy, sell, or otherwise act in-game
- place orders
- perform account automation
- hide monetization logic behind gameplay actions

It is an informational sidebar only.

---

## Why the backend exists

The backend exists so the plugin can fetch precomputed trap judgments and supporting context from the TrapEdge engine.

The goal is to keep the RuneLite client thin and keep the plugin itself understandable.

---

## Preferred Hub-safe submission posture

For a future submission, the preferred review posture is:
- read-oriented panel
- narrow documented network contract
- no writeback required
- clean utility framing
- no platform theater

---

## Reviewer trust rule

The plugin should be understandable as a simple decision-support sidebar, not as an automation client or broad commercial platform shell.
