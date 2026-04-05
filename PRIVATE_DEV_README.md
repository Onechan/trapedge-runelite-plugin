# TrapEdge RuneLite Plugin

Private / sideload-first RuneLite plugin scaffold for TrapEdge.

## What it is
A thin RuneLite sidebar over the TrapEdge judgment engine.

## Current v1 scope
- fetch triage from local TrapEdge plugin API
- show next action
- show compact selected item detail
- show memory notes / matched rules
- support tester sessions before Plugin Hub submission
- support in-client search / confidence filter / action filter / sort
- support status chips and denser in-panel scanning
- keep the plugin client read-only for Plugin Hub prep

## Current UI truth
- the build is now more RuneLite-native visually
- item detail is intentionally more compact and scannable than before
- plugin client writeback has been removed for cleaner Plugin Hub posture

## Hub-safe note
A narrower future submission plan is documented in:
- `../../docs/TRAPEDGE_RUNELITE_PLUGIN_HUB_SAFE_V1_SCOPE.md`
- `../../docs/TRAPEDGE_RUNELITE_PLUGIN_HUB_SAFE_BRANCH_PLAN_V1.md`
- `../../docs/TRAPEDGE_RUNELITE_PLUGIN_HUB_SAFE_FILE_TRIM_CHECKLIST_V1.md`
- `../../docs/TRAPEDGE_RUNELITE_PLUGIN_REVIEWER_NOTE_V1.md`
- `../../docs/TRAPEDGE_RUNELITE_HUB_SAFE_CUT_COMMAND_PACK_V1.md`
- `../../docs/TRAPEDGE_RUNELITE_HUB_SAFE_BRANCH_DIFF_PLAN_V1.md`
- `../../docs/TRAPEDGE_PLUGIN_HUB_PREP_PASS_V1.md`
- `../../docs/TRAPEDGE_PLUGIN_HUB_SUBMISSION_README_V1.md`
- `../../docs/TRAPEDGE_PRIVATE_TESTER_STACK_COMMAND_PACK_V1.md`
- `../../docs/TRAPEDGE_FIRST_TESTER_OUTREACH_PACK_V1.md`

## Local dev
From the TrapEdge repo root:

```bash
npm run plugin:prepare
npm run plugin:api
```

Then in this directory:

```bash
./gradlew test
./gradlew run
```

Or from repo root:

```bash
npm run plugin:run
npm run tester:stack
npm run release:pack
npm run pluginhub:export
npm run local:session
npm run hub-safe:check
```

## Notes
- The scaffold is currently private / sideload-first, but now aligned much closer to Plugin Hub-safe read-only posture.
- `npm run pluginhub:export` builds a standalone Plugin Hub-ready repo shell from the private workspace.
- Gradle toolchain auto-download is enabled so the project can compile even when a full local JDK is missing.
- `./gradlew run` is now wired to the Java 11 toolchain launcher instead of the old Java 8 applet runtime path that previously broke local launch.
