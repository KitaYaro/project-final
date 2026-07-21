## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

## Выполненные задачи

### Задача 1: Структура проекта (onboarding)
- Изучена архитектура проекта JiraRush (Mini-JIRA) — Spring Boot 3.0.2, Java 17
- Разобрана модульная структура: login, profile, bugtracking (task, project, sprint, attachment), mail, ref, common
- Поняты слои: controllers (web), services (internal), repositories, mappers (MapStruct), TO (Transfer Objects)
- Изучена система безопасности: Spring Security с двумя SecurityFilterChain (HTTP Basic для API, Form+OAuth2 для UI)

### Задача 2: Удаление/настройка социальных сетей (Hard task)
- Удалён неработающий провайдер VK: удалён VkOAuth2UserDataHandler.java, конфиг VK из application.yaml и application-prod.yaml
- Оставлены и настроены 4 работающих провайдера OAuth2: GitHub, Google, Yandex, GitLab
- Добавлены scope для GitHub (user:email, read:user), настроен redirect-uri для Yandex и GitLab
- Каждый провайдер имеет обработчик (Strategy pattern): GitHubOAuth2UserDataHandler, GoogleOAuth2UserDataHandler, YandexOAuth2UserDataHandler, GitLabOAuth2UserDataHandler
- Реализован автосоздание пользователей при первом входе через социальную сеть (CustomOAuth2UserService)

### Задача 3: Чувствительная информация вынесена в отдельный проперти файл (Easy task)
- Создан application-secrets.yaml с переменными окружения: JIRA_DB_USERNAME, JIRA_DB_PASSWORD, GITHUB_CLIENT_ID/SECRET, GOOGLE_CLIENT_ID/SECRET, YANDEX_CLIENT_ID/SECRET, GITLAB_CLIENT_ID/SECRET, JIRA_MAIL_USER, JIRA_MAIL_PASSWORD
- application.yaml импортирует application-secrets.yaml через spring.config.import
- Заходящие секреты (пароли OAuth2, пароли БД, почта) удалены из application.yaml
- Создан .env файл с шаблоном переменных окружения для локальной разработки
- Оба файла (.env, application-secrets.yaml) добавлены в .gitignore и удалены из git-индекса (git rm --cached)
- docker-compose.yml передаёт env-переменные в контейнер сервера

### Задача 4: Тесты переведены на in-memory БД H2
- В pom.xml добавлена зависимость H2 с scope=test
- Настроен application-test.yaml: H2 в MODE=PostgreSQL, Liquibase отключён, SQL init mode=always
- Создан changelog-test.sql — полная схема БД адаптированная под H2 (убраны PostgreSQL-специфичные конструкции)
- data.sql адаптирован: добавлены CREATE SEQUENCE IF NOT EXISTS, убраны несуществующие колонки из TASK
- BaseTests аннотирован @ActiveProfiles("test"), AbstractControllerTest загружает схему и данные через @Sql
- Тесты не зависят от внешней PostgreSQL — запускаются из коробки

### Задача 5: Тесты для ProfileRestController
- Написаны 5 тестовых методов для 2 эндпоинтов (GET, PUT):
  - getUnAuth() — проверка 401 Unauthorized без авторизации
  - updateUnAuth() — проверка 401 Unauthorized при PUT без авторизации
  - getProfileIsOk() — проверка 200 OK при авторизованном GET
  - updateProfileIsNoContent() — проверка 204 No Content при валидном PUT
  - updateProfileInvalidUnprocessableEntity() — проверка 422 при невалидных данных
- Используются @WithUserDetails, ProfileTestData с валидными и невалидными данными

### Задача 6: Рефакторинг FileUtil#upload (Easy task)
- Заменены java.io.File и FileOutputStream на java.nio.file.Path, Files.createDirectories(), Files.copy()
- Используется Files.copy(InputStream, Path, StandardCopyOption.REPLACE_EXISTING) вместо multipartFile.transferTo()
- Добавлена защита от path traversal: проверка uploadPath.startsWith(uploadDir)
- Исправлен баг в download(): логическое || заменено на && (resource.exists() && resource.isReadable())
- Удалены неиспользуемые импорты (File, FileOutputStream, OutputStream)

### Задача 7: Добавлен функционал тегов к задачам (REST API + сервис)
- Таблица task_tag уже существовала, теги замаплены в Task.java как @ElementCollection Set<String>
- Добавлено поле tags в TaskToExt (создание/обновление) и TaskToFull (чтение)
- Создан TagService с методами: getTags(taskId), addTag(taskId, tag), removeTag(taskId, tag)
- Создан TagController с REST-эндпоинтами:
  - GET /api/tasks/{id}/tags — получение списка тегов задачи
  - POST /api/tasks/{id}/tags?tag=xyz — добавление тега к задаче
  - DELETE /api/tasks/{id}/tags/{tag} — удаление тега из задачи
- Добавлена валидация: тег от 2 до 32 символов, проверка на дубликаты

### Задача 9: Dockerfile для сервера
- Создан Dockerfile на базе eclipse-temurin:17-jre-jammy (минимальный JRE образ)
- Контейнер копирует собранный Spring Boot jar и запускает его на порту 8080
- ENTRYPOINT настроен для корректной работы с переменными окружения

### Задача 10: docker-compose + nginx (Hard task)
- Создан docker-compose.yml с тремя сервисами:
  - db: PostgreSQL 15 с healthcheck (pg_isready), volume pgdata для персистентности
  - server: Spring Boot приложение, собирается из Dockerfile, Depends_on с condition: service_healthy
  - nginx: реверс-прокси, проксирует /api/ на server:8080, раздаёт статику из /opt/jirarush/resources
- Конфиг nginx включает: gzip, security headers (X-Frame-Options, X-Content-Type-Options, X-XSS-Protection)
- Настроен монтирование config/application-prod.yaml в контейнер сервера
- Переменные окружения передаются через .env файл

### Задача 11: Локализация (минимум 2 языка)
- Созданы два файла сообщений: messages.properties (английский) и messages_ru.properties (русский) — по 29 ключей
- Шаблоны почты (password-reset.html, email-confirmation.html) используют #{...} выражения Thymeleaf
- Стартовая страница index.html и страница login.html полностью локализованы
- MailService использует Locale для выбора языка шаблонов
- Настроено spring.messages.basename: messages с fallback-to-system-locale: false
