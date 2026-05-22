# Рука помощи

«Рука помощи» — веб-приложение для организации социальной и благотворительной помощи.  
Платформа позволяет пользователям размещать заявки, взаимодействовать с организациями и волонтёрами, а также координировать процессы оказания помощи.

## Технологии

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Docker / Docker Compose
- Thymeleaf
- HTML / CSS / JavaScript

## Требования

Перед запуском необходимо установить:

- Java 17+ (или ваша версия Java)
- Maven
- Docker Desktop / Docker Engine

## Запуск проекта

### 1. Клонировать репозиторий

```bash
git clone <ссылка-на-репозиторий>
cd hand-of-help
```

### 2. Запустить PostgreSQL через Docker

В корневой директории проекта выполнить:

```bash
docker compose up -d
```

Будет создан контейнер PostgreSQL со следующими параметрами:

- Database: `education_db`
- User: `postgres`
- Password: `postgres`
- Port: `5435`

Проверить контейнер:

```bash
docker ps
```

### 3. Настроить application.properties

Убедитесь, что настройки подключения совпадают:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5435/education_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. Запустить Spring Boot приложение

Через Maven:

```bash
mvn spring-boot:run
```

или из IDE (IntelliJ IDEA):

Запустить основной класс приложения.

### 5. Открыть приложение

После запуска приложение будет доступно:

```text
http://localhost:8080
```


## Остановка контейнера

Остановить контейнер:

```bash
docker compose down
```

Удалить контейнер и volume:

```bash
docker compose down -v
```

## Автор

Ангелина Джгереная  
Проект «Рука помощи»