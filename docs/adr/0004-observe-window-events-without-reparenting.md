---
status: accepted
---

# Observe window events without reparenting Activity content

FormFiller v2 preserves double-tap and keyboard Fill Triggers, plus the two-finger long-press Scenario Switcher, through a passive `Window.Callback` observer that always delegates to the wrapped callback. This replaces `FormFillerLayout` and Activity content reparenting, works across View and Compose without consuming input, and keeps the switcher as a platform dialog alongside a programmatic Activity fill entry point.
