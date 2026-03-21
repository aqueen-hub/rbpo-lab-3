# Лабораторная работа №3. Подключение базы данных PostgreSQL и JPA

## Цель работы
Перевести проект фитнес-клуба с хранения данных в памяти (in-memory) на работу с реляционной базой данных PostgreSQL с использованием JPA/Hibernate.

## Используемые технологии
- Java 21
- Spring Boot 3.5.12
- Spring Data JPA
- PostgreSQL 18
- Maven

## Структура базы данных
Созданы следующие таблицы (сущности):

1. **members** – участники клуба  
   - id (PK), name, email (unique), phone, registration_date

2. **trainers** – тренеры  
   - id (PK), name, specialization, phone

3. **subscriptions** – подписки участников  
   - id (PK), member_id (FK → members.id), start_date, end_date, type

4. **lessons** – занятия  
   - id (PK), title, trainer_id (FK → trainers.id), start_time, end_time, capacity

5. **bookings** – брони занятий  
   - id (PK), member_id (FK → members.id), lesson_id (FK → lessons.id), booking_time, status

Связи:
- **One-to-Many**: один участник может иметь много подписок; один тренер – много занятий.
- **Many-to-One**: подписка принадлежит одному участнику; занятие – одному тренеру; бронь – одному участнику и одному занятию.

## Настройка подключения
В файле `application.properties` указаны параметры подключения к PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fitness_club
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
