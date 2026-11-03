# sioWE

Android-приложение (Kotlin + Jetpack Compose): вход через Clerk и погода Киева в стиле «жидкое стекло».
Android 15–17 (minSdk 35, compileSdk 37, targetSdk 36).

## Как собрать APK
1. Установите Android Studio (свежую) с Android SDK 37 (SDK Manager → Android 17 / API 37) и JDK 17+.
2. В Clerk Dashboard → **Native applications** включите **Native API** и добавьте package name `com.siowe.app`.
3. В Dashboard → **API keys** скопируйте **Publishable Key** (`pk_test_...`).
4. Скопируйте `local.properties.sample` в `local.properties`, задайте `sdk.dir` и `clerk.publishableKey=pk_test_...`
   (Android Studio сам создаёт `local.properties` с `sdk.dir` — просто добавьте вторую строку).
5. Откройте папку проекта в Android Studio, дождитесь Gradle Sync.
6. **Build → Build Bundle(s) / APK(s) → Build APK(s)**. Файл: `app/build/outputs/apk/debug/app-debug.apk`.
   Или в терминале: `./gradlew assembleDebug` (если нет gradlew — `gradle wrapper` или соберите из Studio).

Погода: Open-Meteo (https://open-meteo.com), ключ не нужен.

## Поведение
- Нет сессии Clerk → экран входа (Clerk `AuthView`). Сессия сохраняется, поэтому вход нужен только при первом запуске.
- Есть сессия → экран погоды. Кнопка профиля (Clerk `UserButton`) позволяет выйти.

## Если Gradle ругается на версии
Версии в `build.gradle.kts` / `app/build.gradle.kts` (AGP 9.0.0, Kotlin 2.4.0, Gradle 9.1.0) подобраны под требования
Clerk SDK 1.2.0. Если Studio предложит более новые — используйте «Upgrade Assistant».
Запасной вариант: `clerk-android-*:1.0.39` + `compileSdk = 36` (тогда нужен AGP/Kotlin, совместимые с этой версией).

## Сборка онлайн (GitHub Actions)
1. Создайте репозиторий на github.com и загрузите в него содержимое папки sioWE (Add file → Upload files).
2. Settings → Secrets and variables → Actions → New repository secret:
   имя CLERK_PUBLISHABLE_KEY, значение pk_test_...
3. Вкладка Actions → Build APK → Run workflow. Через несколько минут откройте запуск и скачайте sioWE-apk внизу страницы (Artifacts).
