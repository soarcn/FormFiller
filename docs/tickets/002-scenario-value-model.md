# Ticket 002: Define the 2.0 Scenario and Value Provider model

- Status: Complete
- Specification: [FormFiller 2.0](../specs/formfiller-2.0.md)
- Blocking tickets: [001](001-modern-build-baseline.md) (complete)

## Scope

Replace the View-specific callback Scenario API with the cross-toolkit FormFiller
2.0 value model. Resolve the Default and selected named Scenario into an ordered,
immutable selector snapshot for each Fill Run, and memoize each matched Value
Provider at that run boundary. Accessibility discovery and Fill Run coordination
remain in later tickets.

## Test seams

- Exercise the public `FormFiller.Builder`, `Scenario.tag()`, `Scenario.id()`,
  `ValueProvider`, and `changeScenario()` configuration API.
- Exercise the internal effective-Scenario boundary as the pure model consumed by
  the future accessibility fill engine; do not fake or expose accessibility
  nodes.

These seams are pre-agreed by the accepted specification's Testing Decisions.

## Acceptance criteria

- `ValueProvider` is a public fun interface returning a non-null `CharSequence`.
- `Scenario.tag()` and `Scenario.id()` each accept either a non-null static Fill
  Value or a Value Provider, including Kotlin trailing-lambda usage.
- The old `EditText` callback overloads and unused hint selector are removed.
- Default and named Scenarios merge deterministically, with named entries
  replacing matching Default entries.
- Effective selector entries are immutable and ordered with Target Tags before
  resource IDs, preserving declaration order within each selector kind.
- A matched Value Provider is evaluated at most once for one effective-Scenario
  snapshot, and the resolved Fill Value is shared by all matching Fill Targets
  and any retry.
- Providers are not evaluated for unmatched entries; a provider failure is
  memoized without retaining its exception and does not prevent unrelated
  entries from resolving.
- Focused unit tests and the repository's full `check assemble` verification
  pass.

## Verification

2026-07-28:

- TDD slices exercised lazy single provider evaluation, immutable static and
  provided Fill Value snapshots, deterministic Scenario merging and selector
  ordering, provider failure isolation, and Scenario capture across a change.
- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew --no-daemon --gradle-user-home /private/tmp/formfiller-gradle :library:testDebugUnitTest`
- Result: `BUILD SUCCESSFUL` (15 tasks).
- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew --no-daemon --gradle-user-home /private/tmp/formfiller-gradle check assemble`
- Result: `BUILD SUCCESSFUL` (174 tasks), including lint, unit tests, and debug
  and release assembly for the library and demo.
- Standards and specification code-review axes reported no remaining findings
  after the pure-model and `View.NO_ID` fixes.
