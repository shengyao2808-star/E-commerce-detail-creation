# Implementation Status

Updated: 2026-05-25 21:10:20 +08:00

## Verified
- Product detail, audit, export, and materials flows run against real backend APIs.
- Audit center `withdraw` and `reaudit` work end to end on a live MariaDB-backed record.
- Frontend audit page loads and renders the real list/detail view.
- Backend starts cleanly after fixing `AIUtil` Spring wiring and enabling Maven `-parameters`.
- Full backend test suite passes: `mvn.cmd clean test` -> 34 tests, 0 failures.

## Current Runtime
- Backend: `8080`
- Frontend: `5173`
- Database: local MariaDB 11.8.6 on `127.0.0.1:3306`

## Still Unimplemented
- PDF export
- OCR
- PDF parsing
- CMS
- SSO
- Tenant isolation
- Full permission model
- Full audit hardening
- Model graph routing
- Market-news automation

## Notes
- `AI_RELAY_*` must be configured before AI generation can be used.
- `withdraw` is a soft delete, so withdrawn audit rows remain in the table with `deleted=1`.
