# AGENTS.md instructions for C:\Users\Administrator\IdeaProjects\E-commerce detail creation

Required workflow for every agent session:

Start of session:
1. Read this `AGENTS.md` first.
2. Read `HANDOFF.md` for the latest snapshot before making changes.
3. For any new requirement, first search GitHub for related repositories/libraries/implementations, then decide what this project should build.

End of session (or before account switch):
1. Update `HANDOFF.md` with current status (what was completed, exact next step, blockers, files touched, and verification).
2. Append a new entry to `HANDOFF_LOG.md` (append-only; do not rewrite prior entries).

Project constraints:
- Always run `npm test` after modifying JavaScript files.
- Prefer `pnpm` when installing dependencies.
- Ask for confirmation before adding new production dependencies.
- If `mvn` is unavailable in PATH, use IntelliJ bundled Maven at `C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.2\plugins\maven\lib\maven3\bin\mvn.cmd`.
