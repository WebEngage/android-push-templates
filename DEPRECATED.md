# ⚠️ This Repository is Deprecated

**Deprecated since:** WebEngage Android SDK v4.8.0

All push notification templates provided in this repository — including Progress Bar, CountDown Timer, and Banner templates — are now natively supported in the [WebEngage Android SDK](https://github.com/WebEngage/android_sdk) v4.8.0 and above (latest v4.21.1).

## Migration

1. Remove the `PushTemplates` module dependency from your project.
2. Upgrade the WebEngage Android SDK dependency to v4.8.0 or later:
   ```gradle
   implementation 'com.webengage:android-sdk:4.+'
   ```
3. Remove the custom callback registrations from your Application class:
   ```kotlin
   // Remove these lines
   WebEngage.registerCustomPushRenderCallback(CustomCallback())
   WebEngage.registerCustomPushRerenderCallback(CustomCallback())
   ```
4. These templates now work out of the box in the core SDK.

## Support

No further updates, bug fixes, or support will be provided for this repository. For any issues, please refer to the [WebEngage Android SDK documentation](https://docs.webengage.com/docs/android-getting-started).
