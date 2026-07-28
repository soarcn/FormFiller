# Ticket 001: Restore the modern build baseline

- Status: Complete
- Specification: [FormFiller 2.0](../specs/formfiller-2.0.md)
- Blocking tickets: None

## Scope

Modernize the Gradle build so the FormFiller 2.0 implementation can begin from
a supported, reproducible Android baseline. This ticket does not implement the
accessibility engine or change the public filling behavior.

## Acceptance criteria

- Kotlin DSL uses Gradle 9.5.0 and AGP 9.3.0 built-in Kotlin.
- The project declares explicit namespaces, `compileSdk 36`, and Java/Kotlin
  JDK 17 toolchains; the demo targets API 36 and both modules retain minSdk 21.
- Repositories contain no JCenter or legacy OSSRH configuration.
- The wrapper runs with JDK 21 while compilation uses the JDK 17 toolchain.
- `check assemble` passes locally, including lint and unit tests.
- CI runs the same `check assemble` verification with JDK 21.
- No source behavior changes are included except compiler/toolchain-required
  compatibility fixes.

## Verification

2026-07-27:

- `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' bash ./gradlew --no-daemon --gradle-user-home /private/tmp/formfiller-gradle check assemble`
- Result: `BUILD SUCCESSFUL` (174 tasks).

2026-07-28:

- Updated the targetSdk 36 demo for Android 15 edge-to-edge enforcement using
  `WindowCompat.enableEdgeToEdge`, `ProtectionLayout`, and a top
  `ColorProtection` backed by `colorPrimaryDark`.
- `check assemble` passed after the compatibility update (174 tasks).

## Notes

`FormFillerLayout` remains until the later trigger-observer ticket removes the
v1 content-reparenting path. Maven Central credentials, signing material, and
publication smoke tests are intentionally deferred to the release ticket.
