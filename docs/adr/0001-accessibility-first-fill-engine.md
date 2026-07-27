---
status: accepted
---

# Use an accessibility-first fill engine while preserving legacy Views

FormFiller v2 keeps the existing artifact and minSdk 21: API 21 through 33 retains EditText discovery, while API 34 and above uses the public in-process accessibility tree to fill exposed nodes with stable identifiers and set-text support. This makes Android 14+ filling independent of View or Compose without adding a Compose dependency or artifact; custom inputs and nodes absent from the current accessibility tree remain outside the guarantee.
