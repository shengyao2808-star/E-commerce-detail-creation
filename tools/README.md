# Tool Library

This directory records the approved external tool library for private deployment.

The project does not vendor large AI/Python repositories directly into the Java
backend. Each tool should be mirrored, pinned, built into a private image or
run as an isolated worker, then called through `/api/v1/tool-adapters`.

Use `tool-library.json` as the source of truth for:

- GitHub repository
- star threshold evidence
- license notes
- integration mode
- default adapter code
- private deployment strategy

Do not add a tool here if it has fewer than 10k GitHub stars unless the product
owner explicitly approves an exception.

