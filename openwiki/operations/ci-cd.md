---
type: Reference
title: NightsOut — CI/CD
description: Continuous integration setup for the NightsOut Android app, including GitHub Actions workflows for builds and wiki updates, Gradle version constraints, and known environment requirements.
---

# CI/CD

NightsOut uses **GitHub Actions** for continuous integration with two configured workflows in `.github/workflows/`.

## Build & Test Workflow (ci.yml)

**Source Path:** [`.github/workflows/ci.yml`](/.github/workflows/ci.yml)

### Job: build-and-test

| Step | Details |
|------|---------|
| **Runner** | `ubuntu-latest` |
| **Java** | Temurin JDK 17 (required by AGP 9.3 / Gradle 9.x) |
| **Cache** | Gradle dependency cache enabled |
| **Assemble** | `./gradlew assembleDebug` — Builds debug APK |
| **Test** | `./gradlew testDebugUnitTest` — Runs unit tests in the JVM (not instrumented) |

### Trigger Branches

| Event | Branches / PRs |
|-------|----------------|
| `push` | `main` branch only |
| `pull_request` | All branches |

## Wiki Update Workflow (openwiki-update.yml)

**Source Path:** [`.github/workflows/openwiki-update.yml`](/.github/workflows/openwiki-update.yml)

This workflow handles automated wiki regeneration by the OpenWiki tool. It runs on a schedule (every two days), updating documentation pages under `/openwiki/` based on source code analysis.

### Configuration

| Setting | Value |
|---------|-------|
| **Schedule** | Every 2 days at 7:00 UTC (`cron: "0 7 */2 * *"`) |
| **Node.js** | v24 |
| **Model** | `qwen3.6-35b` via OpenAI-compatible API |
| **Tracked paths** | `openwiki/` only (AGENTS.md / CLAUDE.md are hand-maintained) |

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
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:9.3.1'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

| Setting | Value | Notes |
|---------|-------|-------|
| **AGP** | 9.3.1 | Modern Android Gradle Plugin — requires JDK 17+ to run |
| **mavenCentral()** | Primary Maven repository | `jcenter()` and `jitpack.io` have been removed; external AARs are vendored locally |

### Vendored Dependencies

The `app/libs/` directory contains pre-jetified AAR files that replaced remote dependencies:

| File | Replaces | Reason |
|------|----------|--------|
| `graphview-4.2.2-androidx.aar` | `com.jjoe64:graphview:4.2.2` | Requires jetification (uses support library `ViewCompat` / `EdgeEffectCompat`) |
| `material-calendarview-2.0.1-androidx.aar` | `com.github.prolificinteractive:material-calendarview:2.0.1` | Jetified version; using local AAR lets us drop `android.enableJetifier` |

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
- **Jetifier dropped:** `android.enableJetifier` is no longer set; dependencies (graphview, material-calendarview) are vendored as pre-jetified AARs in `app/libs/` and resolved via `implementation files()`. This also removed the need for `jitpack.io` repository.
- **No dependency vulnerability scanning:** The CI does not run tools like `dependabot`, `snyk`, or `OWASP Dependency-Check` to identify vulnerable transitive dependencies (e.g., older support library versions).
- **No lint configuration:** No Android Lint checks are run in CI. Running `./gradlew lintDebug` would catch issues like missing permissions, unused resources, and potential crashes.
