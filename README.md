# Parking_area

Десктоп-приложение на JavaFX (Gradle + Java 21).

## Требования
- JDK 21 (другие версии не гарантируются)
- Интернет для первой сборки (Gradle сам скачает зависимости и JavaFX)
- Отдельно устанавливать JavaFX или Gradle **не нужно** — всё подтянется автоматически.

## Запуск

### Windows
```bat
gradlew.bat run
```

### Linux / macOS
```bash
./gradlew run
```

После первой сборки откроется окно приложения.

## Другие команды
| Команда | Описание |
|---|---|
| `gradlew.bat build` / `./gradlew build` | Сборка проекта |
| `gradlew.bat test` / `./gradlew test` | Запуск тестов |
| `gradlew.bat run` / `./gradlew run` | Запуск приложения |

## Запуск из IntelliJ IDEA
1. Открыть папку проекта в IDEA (IDE сама подхватит Gradle-проект).
2. Дождаться импорта зависимостей.
3. Нажать **Run** на конфигурации `Launcher` (main-класс: `org.parking.Launcher`)
   либо выполнить Gradle-задачу `run` на панели **Gradle → Tasks → application → run**.

## Возможные проблемы
- **«unsupported class file major version»** — установлена старая JDK, нужна 21.
- **Антивирус/файрвол блокирует первую сборку** — Gradle скачивает зависимости из Maven Central, нужен доступ в интернет.
- **Кодировка**: проект использует UTF-8; при проблемах с кириллицей убедитесь, что файл `gradle.properties` не менялся.
