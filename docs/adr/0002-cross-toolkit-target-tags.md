---
status: accepted
---

# Reuse Scenario tags across UI toolkits

FormFiller v2 treats the existing `Scenario.tag()` selector as a cross-toolkit Target Tag: it matches Android `View.tag` through the compatibility path and Compose `testTag` through accessibility-node extras. Resource-ID selectors remain View-specific, and no parallel `field()` DSL is added, allowing Compose applications to reuse tags already present for testing.
