---
type: Module Reference
title: NightsOut — Common Dialog Module
description: Detailed reference for the :common-dialog module, which provides reusable AlertDialog components (SimpleDialog with 3 buttons, LightSimpleDialog with 2 buttons) used across NightsOut screens.
---

# Common Dialog Module (`:common-dialog`)

The `:common-dialog` module provides reusable, theme-aware AlertDialog components shared across NightsOut modules. It was introduced to consolidate dialog UI code that was previously duplicated between the app module and the profile module.

## Package Structure

```
/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/
├── SimpleDialog.kt              ← Full-featured dialog (title, body, 3 buttons)
└── LightSimpleDialog.kt         ← Confirmation dialog (title, body, positive/negative only)
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `:common` | Shared constants, string resources, `appThemeToDialogTheme` mapping |
| `androidx.appcompat:appcompat:1.1.0` | AlertDialog.Builder |
| `androidx.core:core-ktx:1.2.0` | Kotlin extensions |

## SimpleDialog

**Source Path:** [`SimpleDialog.kt`](/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/SimpleDialog.kt)

A full-featured dialog with title, body text, and three optional action buttons (positive, negative, neutral).

### Constructor

```kotlin
class SimpleDialog(
    private val context: Context,
    layoutInflater: LayoutInflater,
    parent: View
)
```

The constructor inflates the shared `R.layout.dialog_simple` layout and binds references to the title TextView, body TextView, and three Button views.

### show() Method Signature

```kotlin
fun show(
    title: String,
    body: String,
    positiveButtonText: String = context.getString(R.string.yes),
    positiveAction: (() -> Unit)? = null,
    negativeButtonText: String = context.getString(R.string.no),
    negativeAction: (() -> Unit)? = null,
    neutralButtonText: String = context.getString(R.string.dismiss),
    neutralAction: (() -> Unit)? = null
)
```

### Behavior

- A button is **only visible** when its action lambda is non-null. All three buttons default to "yes", "no", and "dismiss" text respectively, but any can be customized or hidden by passing `null` for the action.
- Dialog dismissal (`dismiss()`) is handled separately via the public `dismiss()` method.

### Typical Usage Pattern

```kotlin
val simpleDialog = SimpleDialog(context, layoutInflater, parent)
simpleDialog.show(
    title = "Clear Session?",
    body = "This will remove all drinks from your current session.",
    positiveButtonText = "Yes",
    positiveAction = { /* clear drinks */ },
    negativeButtonText = "No",
    negativeAction = { simpleDialog.dismiss() }
)
```

## LightSimpleDialog

**Source Path:** [`LightSimpleDialog.kt`](/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/LightSimpleDialog.kt)

A simplified confirmation dialog with only positive and negative buttons. Used for quick confirmations where a neutral action is unnecessary (e.g., "Are you sure?" prompts).

### Usage Pattern

```kotlin
LightSimpleDialog(context, layoutInflater, parent).show(
    title = "Confirm",
    body = "Delete this drink from favorites?",
    positiveAction = { /* delete */ },
    negativeAction = { dismiss }
)
```

The exact constructor and method signatures follow the same pattern as `SimpleDialog` but without the neutral button parameter.

## Dialog Layout Resource

Both dialogs use shared layout XML resources provided by the `:common` module:

| Resource | Purpose |
|----------|---------|
| `R.layout.dialog_simple` | Full dialog with title, body, 3 buttons |

## Theme Integration

**Source Path:** [`ConversionUtils.kt`](/common/src/main/java/com/fagerberg/jason/common/utils/ConversionUtils.kt) — `appThemeToDialogTheme` mapping

The module relies on the `:common` module's `appThemeToDialogTheme` map to select appropriate dialog themes based on the active app theme:

| App Theme | Dialog Theme |
|-----------|-------------|
| `R.style.AppTheme` (light) | `R.style.AppTheme` |
| `R.style.DarkAppTheme` | `android.R.style.Theme_Material_Dialog_Alert` (API 21+) or `R.style.DarkDialog` (pre-Lollipop) |

## Source Paths Summary

| Component | Path |
|-----------|------|
| SimpleDialog | [`/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/SimpleDialog.kt`](/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/SimpleDialog.kt) |
| LightSimpleDialog | [`/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/LightSimpleDialog.kt`](/common-dialog/src/main/java/com/fagerberg/jason/common/dialog/LightSimpleDialog.kt) |

## Design Notes & Caveats

- **App module still has its own SimpleDialog and LightSimpleDialog:** The app module's `dialogs/SimpleDialog.kt` and `dialogs/LightSimpleDialog.kt` are legacy copies that predate the common-dialog module. New code should import from `:common-dialog`, but existing code in both the app and profile modules may still reference the old versions. Verify imports when adding new dialogs.
- **No string resources defined locally:** Dialog button text defaults use string resources from the `:common` module (e.g., `R.string.yes`, `R.string.no`, `R.string.dismiss`). These strings must exist in `:common`'s resources, otherwise compilation will fail when this module is used independently.
- **AlertDialog dependency only:** The module uses only `androidx.appcompat.widget.AlertDialog.Builder`. No Material Components dialogs are used. If Material Design 3 dialog styling is needed in the future, this module would need to add the material dependency and update layouts accordingly.
