# Form Filling

This context describes developer-defined data sets and the application inputs that FormFiller may populate during development.

## Language

**Fill Target**:
An application input exposed through Android's accessibility tree with a stable identifier and the ability to accept replacement text. FormFiller 2.0 can fill a target only on API 34 and above.
_Avoid_: Control, widget, View, Composable

**Target Tag**:
A developer-defined stable string that identifies zero or more Fill Targets across UI toolkits. Every currently exposed match receives the same Fill Value, and Target Tag matching takes precedence over resource-ID matching.
_Avoid_: Field key, test ID, selector string

**Fill Value**:
The replacement text that a Scenario assigns to a Fill Target.
_Avoid_: Payload, input data

**Value Provider**:
A function that produces a Fill Value at most once for each matched Scenario entry during a fill. Its result is shared by every Fill Target matched by that entry.
_Avoid_: Callback, EditText callback, generator

**Fill Diagnostic**:
A redacted Logcat record describing selector matching and best-effort fill outcomes without exposing Fill Values or provider exception messages.
_Avoid_: Fill report, result object

**Fill Trigger**:
A developer action that asks FormFiller to apply the current Scenario to the current Activity.
_Avoid_: Gesture, event hook

**Fill Run**:
The work started by one Fill Trigger for one Activity, including target discovery, Fill Value resolution, at most one retry, and redacted diagnostics. An Activity has at most one active Fill Run and one latest-wins pending Fill Run.
_Avoid_: Fill session, fill request, operation

**Scenario**:
A named set of Target Tags or resource IDs and the Fill Values assigned to them.
_Avoid_: Data set, bullet

**Scenario Switcher**:
The developer-facing chooser that changes the current Scenario before a fill.
_Avoid_: Form switcher, configuration dialog
