# TrapEdge Plugin Hub Prep Pass V1

_Date: 2026-04-05_

## Purpose

Document the concrete changes made to move TrapEdge toward official RuneLite Plugin Hub readiness.

---

# Code changes made

## 1. Client is now read-only
Removed client writeback behavior from:
- `TrapEdgeApiClient.java`
- `TrapEdgePanel.java`
- `TrapEdgeConfig.java`

That means:
- no `POST /api/plugin/postmortem` usage in the plugin client
- no postmortem capture form in the panel
- no Hub-safe toggle needed to hide risky UI

## 2. Plugin copy simplified
Updated:
- plugin descriptor description
- `runelite-plugin.properties`

Current framing is narrower and easier to review:
- trap warnings
- next action
- OSRS flips

## 3. Reviewer-risk surface reduced
Removed or reduced:
- writeback capture
- dual-mode private/hub-safe presentation
- extra reviewer ambiguity around client behavior

---

# Submission-material changes made

## Added
- `TRAPEDGE_PLUGIN_HUB_SUBMISSION_README_V1.md`
- `TRAPEDGE_PLUGIN_HUB_PREP_PASS_V1.md`
- export script for a standalone Plugin Hub-ready repo shell

---

# Remaining truth

This is now much closer to Plugin Hub posture, but not magically approved.

We still need:
- a standalone public plugin repo shell
- final README / LICENSE / resources in that shell
- final submission PR into `runelite/plugin-hub`

---

# One-line rule

> Plugin Hub readiness means making TrapEdge easier to trust, not making it look more impressive.
