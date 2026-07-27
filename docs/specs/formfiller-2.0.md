# FormFiller 2.0: Accessibility-based View and Compose Filling

Status: Ready for implementation (`ready-for-agent`)

## Problem Statement

FormFiller currently discovers concrete `EditText` instances by wrapping and traversing an Activity's View hierarchy. That approach cannot fill Compose inputs, because Compose exposes editable controls as accessibility virtual nodes rather than child Views. It also couples the public Scenario API to `EditText` callbacks and changes Activity content structure in order to observe triggers.

Library users need one development-only form-filling workflow that works for currently exposed View and Compose text inputs on Android 14 and above. They should be able to reuse a stable tag already present in each toolkit, receive predictable Scenario and dynamic-value behavior, and retain the existing gesture, keyboard, and Scenario-switching workflows without the library consuming application input or introducing a Compose runtime dependency.

The library must remain safe for applications whose `minSdk` is lower than API 34. On an unsupported runtime it must not traverse Views or partially fill a form; it must instead report a redacted diagnostic. FormFiller 1.x remains the legacy option for View filling on Android versions below Android 14.

## Solution

Release FormFiller 2.0.0 as a single, Compose-free artifact that fills through Android's public in-process accessibility tree on API 34 and above. The same engine discovers and fills both View and Compose Fill Targets, provided that a target is currently exposed, has a stable identifier, and accepts the set-text accessibility action.

`Scenario.tag()` becomes the cross-toolkit Target Tag selector. View inputs identify themselves with a View tag and Compose inputs with `Modifier.testTag`; both are resolved from the accessibility tree. Resource-ID selectors remain supported when an accessible node exposes a resource ID. A Target Tag takes precedence over a resource ID, and every currently exposed target sharing that tag receives the same Fill Value once per Fill Trigger.

Static Fill Values and public Value Providers replace `EditText` callbacks. The public `fill(Activity)` method is the primary programmatic seam. Gesture and keyboard Fill Triggers, plus the Scenario Switcher, observe the Activity through a delegating `Window.Callback` without reparenting content. Outcomes are best effort and observable through redacted Logcat diagnostics only.

Each Fill Trigger starts a Fill Run for one Activity. A per-Activity coordinator permits one active Fill Run and one pending Fill Run; a later trigger replaces the pending run and starts after the active run, including its bounded retry, has finished. Each run captures its effective Scenario, then memoizes a Fill Value only when its entry first matches, so an intervening Scenario change cannot alter work already in progress or its retry.

## User Stories

1. As an Android developer, I want to include FormFiller 2.0 in an application with `minSdk 21`, so that a development helper does not force my application to raise its minimum SDK.
2. As an Android developer running Android 14 or newer, I want a Fill Trigger to populate exposed View text inputs, so that existing View screens can use the new release without a separate engine.
3. As an Android developer running Android 14 or newer, I want a Fill Trigger to populate exposed Compose text inputs, so that Compose screens receive the same development workflow as View screens.
4. As an Android developer, I want to use `Scenario.tag()` with a View tag, so that a readable stable Target Tag can address one or more View Fill Targets.
5. As an Android developer, I want to use `Scenario.tag()` with `Modifier.testTag`, so that I can reuse the identifier already used by my Compose tests and tooling.
6. As an Android developer, I want a Target Tag to fill every currently exposed matching Fill Target, so that repeated fields can receive one consistent Fill Value.
7. As an Android developer, I want Target Tag matching to take precedence over resource-ID matching, so that overlapping selectors have a deterministic result.
8. As an Android developer, I want each Fill Target to be filled at most once in a Fill Trigger, so that overlapping selectors do not duplicate actions or overwrite a higher-priority Fill Value.
9. As an Android developer, I want to keep resource-ID Scenario selectors where the accessibility node exposes an ID, so that existing selector intent remains usable where the platform can represent it.
10. As an Android developer, I want static Fill Values to be non-null, so that every configured Scenario entry has an explicit replacement value.
11. As an Android developer, I want a Value Provider for dynamically generated text, so that generated names, dates, and tokens can be resolved at fill time without exposing platform controls.
12. As an Android developer, I want each matched Value Provider to run once per Fill Trigger, so that every Fill Target matched by its Scenario entry receives the same resolved Fill Value.
13. As an Android developer, I want the Value Provider snapshot reused by the bounded retry, so that retrying discovery cannot generate inconsistent values.
14. As an Android developer, I want a failing Value Provider to leave unrelated Scenario entries running, so that one dynamic value cannot prevent the rest of a form from being filled.
15. As an Android developer, I want named Scenarios to merge predictably with the Default Scenario, so that I can define common values once and override selected values in a named Scenario.
16. As an Android developer, I want to select a named Scenario programmatically, so that my development workflow can choose data before filling.
17. As an Android developer, I want to call `fill(Activity)` directly, so that automation and demo tests can trigger filling without relying on touch delivery.
18. As an Android developer, I want configured keyboard triggers to continue working, so that hardware-keyboard workflows remain available.
19. As an Android developer, I want the double-tap trigger to observe input without consuming it, so that FormFiller does not change application gesture behavior.
20. As an Android developer, I want the two-finger long-press Scenario Switcher to remain available, so that I can select a Scenario during exploratory testing.
21. As an Android developer, I want FormFiller to preserve the application's Window callback behavior, so that installing the library does not duplicate dispatch or break Activity lifecycle transitions.
22. As an Android developer on API 21 through 33, I want a Fill Trigger to safely do nothing and report why, so that I do not see hidden View traversal or ambiguous partial results.
23. As an Android developer, I want unsupported-platform, unmatched-target, and exhausted-fill diagnostics to be redacted, so that development logs do not expose Fill Values or provider exception messages.
24. As an Android developer, I want a library artifact with no Compose dependency, so that adding Compose support does not alter my application's dependency graph.
25. As a library maintainer, I want a parallel View and Compose demo using one Scenario, so that the cross-toolkit contract stays concrete and demonstrable.
26. As a library maintainer, I want API 34 device tests to exercise the public fill entry point against real accessibility nodes, so that compatibility is proven by observable behavior rather than internal implementation tests.
27. As a library maintainer, I want a local consumer smoke test, so that the published artifact is validated as a consumer would resolve it.
28. As a library maintainer, I want an ABI check before release, so that FormFiller 2.0's breaking public API is deliberately frozen and later changes are visible.
29. As an Android developer, I want overlapping Fill Triggers for one Activity not to execute concurrently, so that a Fill Target is not written by competing Fill Runs.
30. As an Android developer, I want a later Fill Trigger after changing Scenario to run after the current Fill Run, so that my latest explicit action is not lost.
31. As a library maintainer, I want Fill Value resolution and diagnostic redaction to be centralized, so that retry and platform handling cannot apply inconsistent policy.

## Implementation Decisions

- FormFiller 2.0.0 retains the existing Maven coordinate and `minSdk 21`; it is loadable on API 21 through 33 but has no fill capability there.
- A Fill Run is the complete work of one Fill Trigger for one Activity. A per-Activity Fill Run Coordinator owns run state, captures the effective Scenario at trigger time, schedules its retry, and releases state after completion.
- An Activity has one active Fill Run and one pending Fill Run. A later trigger replaces the pending run, and that latest pending run begins only after the active run and its retry finish; Fill Runs for one Activity never execute concurrently.
- API 34 and above uses Android's public in-process accessibility-tree query API as the only discovery and set-text execution path. There is no legacy `EditText` traversal engine in 2.0.
- Each Fill Trigger obtains a fresh accessibility tree and retains no accessibility nodes after the trigger completes.
- The accessibility engine supports exposed View and Compose Fill Targets without referencing Compose classes. A target must expose a stable identifier and a set-text accessibility action.
- `Scenario.tag()` is the Target Tag API. It resolves View tags and Compose test tags from accessibility extras. Resource-ID matching remains available when present in the accessibility tree.
- Tag resolution has precedence over resource-ID resolution. The engine de-duplicates physical targets so no target receives multiple set-text actions in one trigger.
- Scenario entries accept either a non-null static Fill Value or a public `ValueProvider` returning text. EditText callbacks and the unused hint selector are removed.
- Effective Scenario resolution is an immutable internal implementation: it deterministically merges the default and named Scenarios, orders selector entries, preserves Target Tag precedence, and owns per-entry resolved-value or redacted-provider-failure state.
- When an entry first matches, the Fill Run resolves its Value Provider once and stores the result. The same stored Fill Value or provider failure is used for every matching target and both accessibility discovery passes; retry and target execution never invoke a Value Provider.
- Discovery uses at most two passes. The second pass is scheduled on the next frame only for a Scenario entry with no successful first-pass fill. A partially successful multi-target entry does not retry.
- One accessibility fill engine owns node discovery, selector matching, target de-duplication, node lifetime, and set-text action execution. It returns no accessibility nodes across an internal seam and no fake-only accessibility adapter seam is introduced.
- Entry-level failures are isolated. Provider failures do not retry; unsupported targets, stale nodes, and unmatched selectors do not stop unrelated entries.
- On API 21 through 33, `fill(Activity)` and every configured Fill Trigger emit one redacted unsupported-platform Logcat diagnostic and take no discovery or fill action.
- `fill(Activity)` is the public programmatic entry point. It has no public result object; a single internal diagnostics module owns Logcat severity, aggregation, and redaction, and never emits Fill Values or provider exception messages.
- Activity input observation moves from content wrapping to a delegating `Window.Callback`. The wrapper always delegates to the wrapped callback exactly once and is restored safely when no longer applicable.
- The existing keyboard and double-tap Fill Triggers, named Scenario selection, and platform-dialog two-finger long-press Scenario Switcher are retained.
- The published library and its public API remain Compose-free. Compose dependencies are restricted to the demo and test fixture.
- The build moves to Kotlin DSL, Gradle 9.5, Android Gradle Plugin 9.3.0 built-in Kotlin, JDK 17 toolchains, `compileSdk 36`, and demo `targetSdk 36`.
- Version 2.0.0 is a deliberate breaking release. It is tagged `v2.0.0` and published through Maven Central Portal rather than JCenter or legacy OSSRH.

## Testing Decisions

- The primary behavioral seam is the public `FormFiller.fill(Activity)` entry point. End-to-end tests must assert the final application state, not the library's tree traversal, callback wrapper, or accessibility-node retention strategy.
- A single API 34 managed-device fixture contains parallel View and Compose login forms with matching Scenario data. The View form uses View tags; the Compose form uses `Modifier.testTag`. Both are filled through the same public entry point and accessibility engine.
- The API 34 test suite verifies static and provider-generated values, multiple Target Tag matches, tag-over-ID precedence, per-trigger de-duplication, and the bounded retry outcome through observable field values and redacted diagnostics.
- The API 34 test suite verifies that overlapping triggers for one Activity do not execute concurrently, a later trigger replaces an existing pending run, and the pending run uses the Scenario captured when that trigger occurred.
- API 21 through 33 tests verify that a Fill Trigger writes no values, does not start View discovery, and emits only the redacted unsupported-platform diagnostic.
- Unit tests cover the pure Scenario and resolved-value model: default/named Scenario merging, selector precedence, Value Provider single evaluation per matched entry, snapshot reuse, and failure isolation.
- Unit tests cover immutable effective Scenario resolution and the per-Activity Fill Run Coordinator's active/pending/retry state transitions without testing accessibility-node implementation details.
- Trigger tests verify externally observable delegation: configured events are observed without FormFiller consuming input, callbacks are not stacked across lifecycle changes, and the Scenario Switcher does not crash when a dialog cannot be shown.
- Release validation runs unit tests, lint, ABI validation, API 34 managed-device tests, Maven Local publication, and a clean consumer smoke test. Artifact inspection confirms that the AAR and POM contain no Compose dependencies.
- Existing tests for Builder configuration, Scenario merging, and View filling are prior art for the model-level tests. New tests must raise the seam to public Activity behavior instead of extending internal `EditText` mocks as the production design no longer exposes that boundary.

## Out of Scope

- Filling any target on API 21 through 33.
- A compatibility `EditText` traversal engine in FormFiller 2.0.
- Compose Test, direct SemanticsOwner access, RootForTest, reflection, or Compose internal APIs.
- A Compose-specific artifact or a Compose dependency in the published library.
- AccessibilityService, system AutofillService, or autonomous/background form monitoring.
- Custom inputs that do not expose a stable identifier and the set-text accessibility action.
- Public accessibility-node APIs, arbitrary target callbacks, or a public Fill Report.
- Library-enforced debug or release gating; consuming applications remain responsible for when FormFiller is enabled.
- More than one next-frame discovery retry or any persistent accessibility-node cache.
- Multiple concurrent Fill Runs for one Activity, an unbounded pending-run queue, or an accessibility-tree adapter introduced solely to fake Android nodes in tests.

## Further Notes

- The API 34 requirement is a runtime capability requirement, not a dependency requirement. Keeping `minSdk 21` lets a consumer include the helper in a broader application while receiving a clear, safe diagnostic on unsupported devices.
- Compatibility with legacy Android View filling is provided by the published 1.x line, not by a second 2.0 engine.
- The Demo is a release fixture as well as documentation: its View and Compose forms must use the same Target Tags and Scenario definitions wherever the toolkits permit.
- Before implementation begins, split this spec into local tracer-bullet tickets under `docs/tickets/`, with explicit blocking relationships. Work blockers first and give each implementation ticket a fresh context.
