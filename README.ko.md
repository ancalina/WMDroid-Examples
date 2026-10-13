# WMDroid 예제

[English](README.md)

공개 WMDroid SDK AAR를 사용하는 Android 앱 세 개를 제공합니다. 이 저장소에는 SDK 구현 소스와 AAR를 포함하지 않습니다.

| 프로젝트 | Android package | 내용 |
| --- | --- | --- |
| `sample/` | `me.ancal.wmdroid.sample` | 공용 컨트롤, 셸, Today 제공자(provider) |
| `widgets/` | `me.ancal.wmdroid.widgets` | 날씨, 대중교통, 공개 Splatoon 3 일정, 배터리 Today 모듈 |
| `today-probe/` | `org.example.todayprobe` | 카운터와 상태 표시용 Today 제공자 |

## UI 실험실

[WMDroid-Examples.apk](https://github.com/ancalina/WMDroid-Examples/releases/latest)를 설치하고 **WMDroid SDK sample → UI 실험실**을 여세요. 기존 예제와 Today 카운터도 사용할 수 있습니다.

실험실에는 항목 47개가 있습니다. 런처의 글꼴, 테마와 리소스팩을 따릅니다. 런처 없이 실행하면 SDK 기본값을 사용합니다.

| 예제 | 구성 |
| --- | --- |
| 입력 | 텍스트, 버튼, 확인란, 세 상태 확인란, 라디오 버튼, 선택기, 날짜/시간 입력, 범주 |
| 탐색 | 탭, 마법사, 팝업 메뉴, 하위 메뉴, 입력 대화상자, 메시지 상자, 스크롤, 핑거마우스 |
| Today | 행, 여러 줄 텍스트와 구분선 정책 |
| PIM | 월/주/목록/연도 달력, 약속/작업/되풀이 편집, 미리 알림, 전화, 단축 다이얼, 연락처, 메모와 PWI |
| 문서 | 폴더 관리와 문서 속성 |
| 브라우저 | 로컬 HTML 입력, 즐겨찾기와 폴더 편집 |
| 미디어 | 직접 생성한 5초 음원, 재생 목록, 라이브러리와 속성 |
| API 색인 | 공개 최상위 타입 110개의 소스 설명, 공개 선언과 중첩 타입. 관련 예제로 바로 이동 가능 |

색인은 SDK 선언을 보여주며, 모든 메서드를 실행하지는 않습니다. 구형 Android에 없는 플랫폼 타입을 참조하는 선언은 시그니처 대신 오류를 표시합니다. 편집 예제는 가상 데이터를 사용합니다. 새 입력 예제를 닫으면 편집 내용은 버립니다. 기존 예제의 저장 동작은 유지합니다. 전화 예제는 발신하지 않습니다. 브라우저는 로컬 HTML로 시작합니다. 음원은 예제용으로 직접 생성했으며 Microsoft/OEM 미디어를 포함하지 않습니다. PWI 템플릿은 런처 리소스팩이 필요합니다.

SDK를 바꾸면 해당 소스에서 색인을 다시 생성하고 AAR과 대조하세요.

```sh
python3 tools/generate_api_catalog.py /path/to/sdk/src/main/java/me/ancal/wmdroid/sdk
python3 tools/check_api_catalog.py libs/WMDroid-sdk.aar
```

## 빌드

공개 `WMDroid-sdk.aar`를 저장소의 최상위 디렉터리에 있는 `libs/WMDroid-sdk.aar`로 복사하세요. Java 17, Android SDK `android-35`, Android Gradle Plugin `8.7.3`과 호환되는 Gradle이 필요합니다. Gradle wrapper는 포함하지 않습니다. `ANDROID_HOME` 또는 로컬 `local.properties`로 Android SDK 경로를 설정하세요.

```sh
gradle :sample:app:assembleDebug
gradle :widgets:app:assembleDebug
gradle :today-probe:app:assembleDebug
```

저장소의 최상위 디렉터리에서 실행합니다. 각 APK는 `<project>/app/build/outputs/apk/`에 생성됩니다. 최소 Android API는 14, 대상 API는 35입니다. Gradle은 설정된 저장소에서 버전이 고정된 의존성을 다운로드합니다. 오프라인으로 빌드하려면 해당 의존성이 Gradle 캐시에 있어야 합니다. `desugar_jdk_libs:2.1.5`와 `androidx.multidex:multidex:2.0.1`을 사용합니다.

필요한 예제를 WMDroid와 함께 설치하고 Today → 항목에서 제공자를 켜세요. 각 제공자는 사용자가 개별적으로 선택해야 활성화됩니다. Widgets의 데이터 소스에는 사용자 설정이나 인증키가 필요할 수 있습니다. 실제 인증키를 소스나 제공자의 출력에 넣지 마세요. Microsoft/OEM 리소스팩은 포함하지 않습니다. `WmApplet`은 호스트가 선택한 팩을 비동기로 반영합니다.

## 라이선스

WMDroid가 작성한 예제 코드와 프로젝트 파일은 [WTFPL v2](LICENSE)를 따릅니다. 각 프로젝트의 `LICENSE`와 [NOTICE](NOTICE)를 유지하세요. SDK는 Apache-2.0과 해당 고지를 유지하며, 의존성은 각자의 라이선스를 따릅니다. 의존성 고지는 `runtime-assets/third_party/`에 있습니다. 이 예제가 Windows Mobile과의 완전한 동작 및 화면 일치나 모든 Android 버전에서의 실시간 네트워크 서비스 호환성을 입증하는 것은 아닙니다.
