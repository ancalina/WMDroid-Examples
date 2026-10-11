# WMDroid examples

[한국어](README.ko.md)

Three Android apps consume the published WMDroid SDK AAR. SDK implementation sources and the AAR are not included in this repository.

| Project | Android package | Contents |
| --- | --- | --- |
| `sample/` | `me.ancal.wmdroid.sample` | Shared controls, shell and a Today provider |
| `widgets/` | `me.ancal.wmdroid.widgets` | Weather, transit, public Splatoon 3 schedules and battery Today modules |
| `today-probe/` | `org.example.todayprobe` | Counter and status Today providers |

## Build

Copy the published `WMDroid-sdk.aar` to `libs/WMDroid-sdk.aar` at this repository's root. Use Java 17, Android SDK `android-35`, and Gradle compatible with Android Gradle Plugin `8.7.3`. No Gradle wrapper is included. Configure the Android SDK through `ANDROID_HOME` or a local `local.properties` file.

```sh
gradle :sample:app:assembleDebug
gradle :widgets:app:assembleDebug
gradle :today-probe:app:assembleDebug
```

Run from the repository root. Each APK is written to `<project>/app/build/outputs/apk/`. The minimum Android API is 14 and target API is 35. Gradle downloads pinned dependencies from its configured repositories; offline builds need an existing Gradle cache. The projects use `desugar_jdk_libs:2.1.5` and `androidx.multidex:multidex:2.0.1`.

Install the desired example alongside WMDroid. Enable its providers in Today → Items. Each provider has its own opt-in toggle. Widgets data sources may need user configuration or credentials; do not put real credentials in source files or provider output. Microsoft/OEM resource packs are not bundled. `WmApplet` follows the host's selected pack asynchronously.

## License

WMDroid-authored example code and project files use [WTFPL v2](LICENSE). Preserve each project's `LICENSE` and [NOTICE](NOTICE). The SDK retains Apache-2.0 and its applicable notices; dependencies keep their own licenses. Dependency notices are under `runtime-assets/third_party/`. These examples do not establish full Windows Mobile parity or live network-service compatibility on every Android version.
