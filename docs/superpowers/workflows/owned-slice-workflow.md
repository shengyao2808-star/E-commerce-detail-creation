# Owned Slice Workflow

Use this workflow when a session is taking a bounded backend, frontend, or full-stack slice in this repository and must leave behind a trustworthy handoff.

## Why this exists

This repository repeated the same manual loop across `HANDOFF.md` and `HANDOFF_LOG.md` on 2026-05-25 through 2026-05-27:

- read `AGENTS.md` and the latest handoff snapshot first
- scope a narrow owned slice
- align backend contract, frontend usage, and docs without inventing data
- run the exact verification required by the touched surface
- record the next step, blockers, touched files, and exact commands

Examples include:

- `Coordinator / P2 backend task APIs and persistence`
- `Coordinator / detail composition auto-assembly slice`
- `Coordinator / P3.6.1 visual planning persistence base`
- `Backend P3.7 prompt workbench normalization + route alignment`

## Reuse before creating

Use existing installed skills for the generic parts:

- `writing-plans` for multi-step implementation plans
- `verification-before-completion` before any success claim
- `dispatching-parallel-agents` or `subagent-driven-development` only when delegation is explicitly requested

Do not create a new generic delegation skill for this repo. The repeated gap here is the repo-specific execution and closeout pattern.

## Inputs

- `AGENTS.md`
- latest `HANDOFF.md` entry
- current dirty tree (`git status --short`)
- exact user request
- only the files inside the owned slice

## Workflow

1. Start from repo truth.
   - Read `AGENTS.md`.
   - Read the newest `HANDOFF.md` entry before changing code.
   - Check the dirty tree so you do not overwrite someone else's work.

2. Define the slice in one sentence.
   - Name the owned surface, for example `backend visual-plan persistence semantics`.
   - State what is out of scope.

3. Trace the minimum real contract path.
   - Backend slices: schema/entity/mapper/service/controller/tests/docs as needed.
   - Frontend slices: API types/client/page state/tests/build as needed.
   - Full-stack slices: change the backend contract first, then remove frontend derivation that only existed to patch around old backend behavior.

4. Preserve the repo's hard rules.
   - Fail closed when external tools or AI are unavailable.
   - Do not fabricate records, statuses, files, progress, or success payloads.
   - Prefer root route aliases and DTO normalization patterns already used in the repo.

5. Verify by touched surface.
   - Java or backend contract changed: run Maven tests, using IntelliJ bundled Maven if `mvn` is unavailable.
   - JavaScript or TypeScript changed: run `cd frontend; npm test`.
   - Frontend page or bundle behavior changed: also run `cd frontend; npm run build`.
   - Doc-only or workflow-only change: run a direct smoke check for the new asset instead of claiming unverified success.

6. Close out with exact evidence.
   - Update `HANDOFF.md` with action, completed work, exact next step, blockers, files touched, and verification.
   - Append a new `HANDOFF_LOG.md` entry. Do not rewrite earlier entries.
   - Record the real command lines and real outcomes, including unrelated blockers when they affected verification.

## Closeout checklist

- Owned slice stayed narrow.
- Existing dirty files outside the slice were left alone.
- Verification matches the touched surface.
- `HANDOFF.md` names the exact next step, not a vague direction.
- `HANDOFF_LOG.md` is append-only.
- Final report distinguishes what was created, extended, skipped, and what still needs evidence.
