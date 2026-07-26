---
type: Reference
title: NightsOut — CI/CD
description: Continuous integration setup for the NightsOut Android app, including GitHub Actions workflows for builds and wiki updates, Gradle version constraints, and known environment requirements.
---

# CI/CD

NightsOut uses **GitHub Actions** for continuous integration with two configured workflows in `.github/workflows/`.

## Build & Test Workflow (ci.yml)

**Source Path:** [`.github/workflows/ci.yml`](/.github/workflows/ci.yml)

### Triggers

| Event | Branches / PRs |
|-------|----------------|
| `push` | `master` branch only |
| `pull_request` | All branches |

### Job: build-and-test

| Step | Details |
|------|---------|
| **Runner** | `ubuntu-latest` |
| **Java** | Temurin JDK 8 (required by AGP 3.6.2 / Gradle wrapper constraints) |
| **Cache** | Gradle dependency cache enabled |
| **Assemble** | `./gradlew assembleDebug` — Builds debug APK |
| **Test** | `./gradlew testDebugUnitTest` — Runs unit tests in the JVM (not instrumented) |

### Java Version Constraint

The workflow explicitly uses JDK 8 despite Gradle wrapper potentially supporting newer versions. This is because:
- AGP 3.6.2 was designed for and tested against JDK 8
- The Gradle wrapper version at project setup (5.6.4 equivalent) supports at most JDK 12
- Using JDK 8 avoids compatibility issues with annotation processing and R8/ProGuard

### Known Constraint: Recent Gradle Upgrade

The commit `9570217 upgrade to gradle 9.6.1` indicates a significant Gradle version bump from the original wrapper (5.x era) to 9.6.1. However, the CI workflow still targets JDK 8 and references AGP 3.6.2. This suggests:
- Either the upgrade was partial (Gradle only, not AGP)
- Or the CI configuration is stale and needs updating for newer Gradle/AGP versions

**Action needed:** Verify that `./gradlew testDebugUnitTest` actually passes with the current Gradle version on JDK 8. If tests fail, update the CI to use JDK 11+ or upgrade AGP/Kotlin to match Gradle 9.x.

## Wiki Update Workflow (openwiki-update.yml)

**Source Path:** [`.github/workflows/openwiki-update.yml`](/.github/workflows/openwiki-update.yml)

This workflow handles automated wiki regeneration by the OpenWiki tool. It runs on a schedule and after pushes to the repository, updating documentation pages under `/openwiki/` based on source code analysis.

**Note:** Do not hand-edit generated OpenWiki pages. Instead, update source code/docs and let OpenWiki regenerate them. See [AGENTS.md](/AGENTS.md) for details.

## Local Build Commands

| Command | Purpose |
|---------|---------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK (no minification enabled) |
| `./gradlew testDebugUnitTest` | Run JVM unit tests (common module only) |
| `./gradlew connectedAndroidTest` | Run instrumented tests on device/emulator |
| `./gradlew installDebug` | Install debug APK to connected device |

### Gradle Configuration

**Top-level build.gradle** (`/build.gradle`):

```groovy
buildscript {
    ext.kotlin_version = '1.3.72'
    repositories {
        google()
        jcenter()  // Deprecated — should migrate to mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.6.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

allprojects {
    repositories {
        google()
        jcenter()  // Deprecated
        maven { url 'https://jitpack.io' }  // For material-calendarview
    }
}
```

| Setting | Value | Notes |
|---------|-------|-------|
| **Kotlin** | 1.3.72 | Older version — newer Kotlin versions (1.5+, 1.9+) have improved coroutines and null-safety |
| **AGP** | 3.6.2 | Older Android Gradle Plugin — consider upgrading to 8.x for modern compile SDK support |
| **jcenter()** | Used in repositories | **DEPRECATED since Feb 2021** — should be replaced with `mavenCentral()` |
| **jitpack.io** | Fetched via maven | Required by `com.github.prolificinteractive:material-calendarview:1.6.0` |

### ProGuard / Minification

Release builds are configured with `minifyEnabled false` across all modules. No custom ProGuard rules exist beyond the defaults provided in `consumer-rules.pro` files. This means release APKs will be larger than necessary and may include debug symbols.

**Recommendation:** Enable R8 minification for release builds and define obfuscation rules to reduce APK size.

## Source Paths Summary

| Resource | Path |
|----------|------|
| CI workflow | [/.github/workflows/ci.yml](/.github/workflows/ci.yml) |
| Wiki update workflow | [/.github/workflows/openwiki-update.yml](/.github/workflows/openwiki-update.yml) |
| Top-level build.gradle | [/build.gradle](/build.gradle) |
| Project settings | [/settings.gradle](/settings.gradle) |
| Gradle wrapper version | Check `/gradle/wrapper/gradle-wrapper.properties` (not yet inspected — should match referenced wrapper version) |

## Design Notes & Caveats

- **No instrumentation tests in CI:** The workflow runs only `testDebugUnitTest`, which excludes Android instrumented tests (`db/src/androidTest/`). These require a connected device or emulator and would need a separate job to run.
- **No code coverage reporting:** Neither workflow generates test coverage reports. Adding JaCoCo or Android's built-in coverage could help track regressions as the codebase evolves.
- **jcenter() deprecation:** All three `allprojects` repositories still reference jcenter(), which has been shut down since February 2021. Dependencies may fail to resolve in new environments unless they are cached locally. Migrate all `jcenter()` references to `mavenCentral()`.
- **No dependency vulnerability scanning:** The CI does not run tools like `dependabot`, `snyk`, or `OWASP Dependency-Check` to identify vulnerable transitive dependencies (e.g., older support library versions).
- **No lint configuration:** No Android Lint checks are run in CI. Running `./gradlew lintDebug` would catch issues like missing permissions, unused resources, and potential crashes.
