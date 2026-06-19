# System Metadata

## Keystore Details
- The Android project uses an automatically generated `debug.keystore` to sign the debug APK.
- The `debug.keystore.base64` file contains the base64-encoded version of this keystore. This ensures the same signing key is preserved across environments.
- **IMPORTANT**: If `debug.keystore.base64` is modified, corrupted, or deleted, a new keystore will be generated. The result is that Android will refuse to update the existing app on the device due to a signature mismatch. This requires a manual uninstall of the previous version before the new version can be installed.

## Output Data
- Compiled application packages are typically stored in the `.build-outputs/` folder (e.g., `app-debug.apk`).
- Sometimes the outputs can glitch if old build artifacts are cached. If the app fails to install, changing the `applicationId` can serve as a workaround for signature mismatches without requiring manual uninstallation on the target device.
