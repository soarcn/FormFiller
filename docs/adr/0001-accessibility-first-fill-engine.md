---
status: accepted
---

# Use one accessibility fill engine on Android 14 and above

FormFiller v2 keeps its existing low minSdk so it remains safe to include as a development helper, but uses the public in-process accessibility tree to fill exposed nodes only on Android 14 / API 34 and above. Below API 34, a Fill Trigger performs no fill and emits a redacted Logcat diagnostic. It does not retain an EditText traversal compatibility path: the published 1.x line remains the option for View filling on older Android releases. This makes v2 filling independent of View or Compose without adding a Compose dependency or artifact; custom inputs and nodes absent from the current accessibility tree remain outside the guarantee.
