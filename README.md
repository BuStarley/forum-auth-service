# Сервис аутентификации

Микросервис для регистрации, входа и управления JWT-токенами.

---

## Функциональность

* Регистрация нового пользователя (`/register`)
* Вход в систему (`/login`) — выдаёт **access** и **refresh** токены
* Обновление access-токена по refresh-токену (`/refresh`)
* Проверка валидности access-токена (`/verify`)

---

## Стек технологий

| Компонент          | Технология                   |
|--------------------|------------------------------|
| Язык               | Java 25                      |
| Фреймворк          | Spring Boot 4.0.3            |
| Безопасность       | Spring Security, JJWT 0.13.0 |
| База данных        | PostgreSQL                   |
| ORM                | Spring Data JPA Hibernate    |
| Миграции БД        | Flyway                       |
| Маппинг DTO        | MapStruct                    |
| Сборщик            | Maven                        |
| CI/CD              | GitLab CI (local runner)     |

## База данных

### Таблица users
| Поле             | Тип           | Описание                       |
|------------------|---------------|--------------------------------|
| `id`             | UUID          | Первичный ключ                 |
| `email`          | VARCHAR(255)  | email пользователя             |
| `password_hash`  | VARCHAR(255)  | Хеш пароля                     |
| `role`           | VARCHAR(50)   | Роль (USER, MODERATOR, ADMIN)  |
| `enabled`        | BOOLEAN       | Активен ли аккаунт             |
| `created_at`     | TIMESTAMP     | Дата регистрации               |
| `updated_at`     | TIMESTAMP     | Дата обновления                |

### Таблица refresh_tokens
| Поле         | Тип          | Описание                 |
|--------------|--------------|--------------------------|
| `id`         | UUID         | Первичный ключ           |
| `user_id`    | UUID         | Внешний ключ к users(id) |
| `token`      | VARCHAR(512) | Refresh токен            |
| `expires_at` | TIMESTAMP    | Когда истекает           |
| `created_at` | TIMESTAMP    | Дата создания            |

## Запуск проекта локально

### 1. Клонирование
```bash
git clone git@gitlab.com:BuStarley/forum-auth-service.git
cd forum-auth-service