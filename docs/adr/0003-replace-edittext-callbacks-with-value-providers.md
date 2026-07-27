---
status: accepted
---

# Replace EditText callbacks with value providers

FormFiller v2 removes the callback API that exposes `EditText` and accepts either non-null static Fill Values or a public `ValueProvider` fun interface. This intentional breaking change keeps cross-toolkit behavior limited to text replacement, avoids exposing short-lived accessibility nodes, and retains trailing-lambda Kotlin usage plus a stable Java-facing provider type.
