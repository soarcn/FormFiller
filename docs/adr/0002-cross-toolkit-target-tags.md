---
status: accepted
---

# Reuse Scenario tags across UI toolkits

FormFiller v2 treats the existing `Scenario.tag()` selector as a cross-toolkit Target Tag: it matches Android View tags and Compose `testTag` values through accessibility-node extras. Resource-ID selectors remain supported when exposed by the same accessibility node, and no parallel `field()` DSL is added, allowing Compose applications to reuse tags already present for testing.
