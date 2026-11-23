# bazaar_updater

A Flutter plugin wrapping Cafe Bazaar's official **BazaarUpdater** Android SDK
(`com.github.cafebazaar:bazaarUpdater`), so you can check for and trigger
in-app updates from Bazaar directly -- no manual DB entry needed on your
backend for the bazaar flavor.

This is the real thing, not a reverse-engineered guess: BazaarUpdater is
Cafe Bazaar's own supported SDK for exactly this purpose.

## Usage

```dart
final state = await BazaarUpdater.checkForUpdate();

switch (state) {
  case BazaarUpdateState.newVersionAvailable:
  case BazaarUpdateState.newVersionDownloaded:
    // show your update prompt, then on tap:
    await BazaarUpdater.updateApplication();
    break;
  case BazaarUpdateState.noNewVersion:
    // already up to date
    break;
  case BazaarUpdateState.unknown:
    // Bazaar not installed, check failed, or signing/package mismatch --
    // fall back to your own backend's check here.
    break;
}
```

If `checkForUpdate()` ever returns `newVersionDownloaded` but the install
didn't happen automatically (some Xiaomi devices block it), call
`BazaarUpdater.installDownloadedUpdate()` while the app is in the
foreground to resume it.

## Things that will bite you if skipped

- **No mandatory update support.** Bazaar's own FAQ says this SDK can't
  force an update. If `is_mandatory` from your backend matters, that logic
  still has to live in your app, separate from this SDK -- this only tells
  you whether an update _exists_.
- **Package name + signing must match exactly** what's published on
  Bazaar. A debug build, or a build signed with a different keystore/alias
  than your release, will never detect a real update -- this is the single
  most common reason people think the SDK is "broken."
- **`newVersionDownloaded` needs Bazaar app >= 30.1.0** on the device.
  Older Bazaar installs can only ever report `newVersionAvailable` or
  `noNewVersion`.
- **`installDownloadedUpdate()` needs the app in the foreground** -- Android
  10+ blocks background apps from launching the system install prompt.

## One note on the native imports

`BazaarUpdaterPlugin.kt` imports `ir.cafebazaar.bazaarUpdater.BazaarUpdater`
and `ir.cafebazaar.bazaarUpdater.model.response.NewVersionState`. These are
my best read of the official doc's code snippets, which show usage but never
an explicit `import` line. Once the dependency resolves, let your IDE's
"fix imports" correct these two lines if they don't match the real package
layout.
