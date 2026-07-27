# FormFiller 2.0 Roadmap

Status: Accepted for implementation

## Objective

Upgrade FormFiller into an accessibility-first form-filling library that supports currently exposed View and Compose text inputs on Android 14 and above, while preserving legacy View filling on Android 5.0 through Android 13.

## Confirmed Product Contract

- Keep the existing `com.cocosw:formfiller` artifact and `minSdk 21`.
- Use Android's public in-process accessibility-tree API on API 34 and above.
- Keep the existing View traversal compatibility path on API 21 through 33 and where View-only metadata requires it.
- Treat `Scenario.tag()` as a cross-toolkit Target Tag: Android `View.tag` for Views and Compose `testTag` for Compose.
- Match and fill every currently exposed node with the same Target Tag.
- Prefer a Target Tag match over a resource-ID match and fill each target at most once per trigger.
- Fill only nodes that accept replacement text; custom inputs that do not expose this capability are outside the guarantee.
- Replace `EditText` callbacks with static Fill Values and Value Providers.
- Evaluate each matched Value Provider at most once per fill and share its result across every node matched by that Scenario entry.
- Emit redacted Logcat diagnostics rather than a public fill-report API.
- Isolate entry failures: provider failures do not retry, accessibility failures receive the bounded second pass, and neither stops unrelated entries.
- Log unmatched entries at debug, exhausted failures at warning, and a redacted count summary after each trigger.
- Leave debug and release gating to the consuming application.
- Preserve the two-finger long-press Scenario Switcher and programmatic Scenario selection.

## Confirmed Architecture

- Do not depend on Compose from the published library artifact.
- Do not publish a separate Compose integration artifact.
- Re-query the accessibility tree for every fill instead of caching accessibility nodes.
- Run at most two accessibility discovery passes per Fill Trigger, retrying on the next frame only for entries with no successful first-pass fill.
- Reuse the same Value Provider snapshot across both discovery passes and do not retry partially successful multi-target entries.
- Observe double-tap and keyboard Fill Triggers through a delegating `Window.Callback`; do not reparent Activity content.
- Provide a programmatic Activity fill entry point.

## Build and Release Baseline

- Release the breaking upgrade as Maven version `2.0.0` and Git tag `v2.0.0`.
- Upgrade to Gradle 9.5 and Android Gradle Plugin 9.3.0.
- Use AGP built-in Kotlin and Kotlin DSL build scripts.
- Use a JDK 17 toolchain, `compileSdk 36`, and `targetSdk 36` for the demo.
- Replace JCenter and legacy Sonatype OSSRH publishing with Maven Central Portal publishing.
- Add Compose only to the demo and test modules so the published library dependency graph remains Compose-free.

## Public API Migration

- Preserve `FormFiller.Builder`, Scenario configuration, Fill Triggers, the Scenario Switcher, and `changeScenario()`.
- Remove every `(EditText) -> Unit` callback overload and remove the unused hint selector.
- Make Fill Values non-null.
- Add a public `ValueProvider` fun interface returning `CharSequence`.
- Provide static-value and Value Provider overloads for both `Scenario.tag()` and `Scenario.id()`.
- Add `FormFiller.fill(Activity): Unit`; fill outcomes remain available only through redacted Logcat diagnostics.

## Validation Fixture

- Keep the View login form and add a parallel Compose login form using the same Scenario and Target Tags.
- Use Android `View.tag` in the View fixture and standard `Modifier.testTag` in the Compose fixture.
- Exercise the public programmatic fill API rather than Compose test APIs when validating the fill path.
- Run API 33 managed-device tests for the legacy View path.
- Run API 34 managed-device tests for both View and Compose accessibility paths.
- Assert final application or accessibility state without depending on `compose-ui-test`.
- Add unit tests for Scenario matching, Value Provider snapshots, merging, and failure isolation.
- Verify that the published AAR and POM remain free of Compose dependencies.

## Implementation Sequence

### 1. Restore a Modern Build Baseline

Outputs:

- Kotlin DSL build scripts using Gradle 9.5 and AGP 9.3.0 built-in Kotlin.
- Explicit namespaces, JDK 17 toolchain, `compileSdk 36`, and no JCenter configuration.
- Updated unit-test dependencies and a locally buildable library and demo.

Tests and acceptance:

- The wrapper runs under the installed JDK 21 and uses the JDK 17 toolchain for compilation.
- `check`, lint, unit tests, and `assemble` pass from a clean checkout.
- No source behavior changes are mixed into the baseline commit unless required by the new toolchain.

### 2. Define the 2.0 Scenario and Value Model

Outputs:

- Non-null static Fill Values and the public `ValueProvider` fun interface.
- Static and provider overloads for `tag()` and `id()`.
- Removal of `EditText` callbacks and the unused hint selector.
- Per-trigger provider snapshots, Scenario inheritance, Target Tag precedence, and multi-match behavior.

Tests and acceptance:

- Each matched provider is evaluated once per Fill Trigger, including across a second accessibility pass.
- All targets sharing a Target Tag receive the same resolved value.
- Target Tag matching takes precedence over resource-ID matching and each target is filled at most once.
- Default and named Scenario merging remains deterministic.

### 3. Implement Discovery and Fill Engines

Outputs:

- Legacy EditText discovery for API 21 through 33 and View-only tag compatibility where required.
- API 34+ in-process accessibility-tree discovery using resource IDs and Compose testTag extras.
- Set-text action execution, per-run target de-duplication, bounded next-frame retry, and redacted Logcat diagnostics.
- Public `fill(Activity): Unit` entry point.

Tests and acceptance:

- Standard View EditText fields remain fillable by ID and tag on API 21+.
- Standard Compose text fields with `Modifier.testTag` are fillable on API 34+ without Compose dependencies in the library.
- A fresh accessibility tree is used for each pass and no accessibility node is retained after the trigger.
- Provider failures, unsupported nodes, stale nodes, and unmatched selectors do not prevent unrelated entries from filling.
- Diagnostics never contain Fill Values or provider exception messages.

### 4. Replace Activity Content Wrapping

Outputs:

- Delegating `Window.Callback` observer for double-tap, keyboard, and two-finger long-press events.
- Removal of `FormFillerLayout` and Activity content reparenting.
- Platform-dialog Scenario Switcher and safe callback restoration at Activity teardown.

Tests and acceptance:

- Every observed event is delegated exactly once and FormFiller never consumes application input.
- Triggers work in both View and Compose demo screens.
- Repeated lifecycle transitions do not stack callback wrappers or leak Activities.
- Dialog display failures are diagnosed without crashing the application.

### 5. Build the Demo and End-to-End Matrix

Outputs:

- Parallel View and Compose login forms using the same Scenario and Target Tags.
- API 33 managed-device legacy View tests.
- API 34 managed-device View and Compose accessibility tests.
- Tests triggered through the public FormFiller API without `compose-ui-test`.

Tests and acceptance:

- Both demo forms receive the expected static and provider-generated values.
- API 34 tests exercise real AccessibilityNodeInfo discovery and set-text actions.
- The Compose fixture demonstrates `Modifier.testTag` as the only FormFiller-specific field requirement.
- Published library metadata contains no Compose dependency.

### 6. Freeze the API and Release 2.0.0

Outputs:

- Kotlin ABI dump and compatibility check integrated with Gradle `check`.
- Updated README, callback-to-provider migration guide, compatibility matrix, and release notes.
- Maven Central Portal publishing through Gradle Maven Publish Plugin 0.37.0.
- Tag-driven GitHub Actions release workflow protected by an approval environment.

Tests and acceptance:

- Pull requests pass unit tests, lint, ABI validation, API 33/34 managed-device tests, and Maven-local consumer smoke tests.
- Generated AAR, sources, javadoc, POM, signatures, coordinates, and version are inspected before release.
- The POM has no Compose dependency and a clean consumer resolves `com.cocosw:formfiller:2.0.0` from Maven Local.
- Tag `v2.0.0` matches Maven version `2.0.0` before publishing.
- Central Portal validation succeeds before the GitHub Release is created.

## Out of Scope for 2.0

- Compose filling below API 34.
- Compose Test, direct SemanticsOwner access, RootForTest, reflection, or Compose internal APIs.
- An AccessibilityService or system AutofillService integration.
- A Compose dependency or separate Compose artifact in the published library.
- Custom inputs that do not expose a stable identifier and set-text accessibility action.
- Public accessibility nodes, arbitrary target callbacks, or a public FillReport.
- Library-enforced debug or release gating.
- Unbounded retries or continuously monitoring the accessibility tree.

## Delivery Risks and Controls

- **Callback chaining:** wrap and restore the exact current `Window.Callback`; verify no duplicate delegation across lifecycle changes.
- **Stale accessibility nodes:** use a fresh tree per pass, retain no nodes, and allow only one bounded retry.
- **Selector overlap:** preserve Target Tag precedence and de-duplicate targets before applying actions.
- **Incorrect Compose tagging:** require the testTag to identify the editable node and diagnose nodes without set-text support.
- **Two-engine compatibility:** exercise the same View form on API 33 and API 34 to detect behavior drift.
- **Emulator reliability:** use Gradle-managed AOSP devices and software rendering in GitHub Actions.
- **Release safety:** require ABI review, Maven-local consumer validation, signed Central Portal validation, and protected-environment approval.
