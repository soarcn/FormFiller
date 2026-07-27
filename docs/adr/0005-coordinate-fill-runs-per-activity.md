---
status: accepted
---

# Coordinate Fill Runs per Activity

FormFiller v2 treats a Fill Trigger as a Fill Run that captures the current Scenario when it starts. An Activity has at most one active Fill Run and one pending Fill Run; a later trigger replaces the pending run, which starts after the active run and any bounded next-frame retry complete. This prevents concurrent writes to the same Fill Target while preserving an explicit later trigger made after a Scenario change. The coordinator owns run state, retry scheduling, and state release; its policy is shared by programmatic, keyboard, and double-tap triggers.
