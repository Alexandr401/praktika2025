# Информационно-справочная система художественной галереи

Данный проект представляет собой настольное Java-приложение, предназначенное для хранения, обработки и отображения информации о художниках, произведениях искусства и выставках художественной галереи.

## Используемые технологии

- Java 21
- JavaFX 21
- Maven
- Hibernate ORM (JPA)
- PostgreSQL
- BCrypt (хэширование паролей)
- SLF4J (логирование)


--- 
## Запуск готового приложения (GalleryApp.exe)

## Требования 
- PostgreSQL 17+

### Настройка
1. Распаковать архив 
2. Создать базу данных ```sql CREATE DATABASE art_galleries_db; ```

3. Выполнить SQL-скрипт init.sql (инициализация данных, без него приложение будет без данных) 
4. Запустить GalleryApp.exe.

---

## Запуск проекта в IntelliJ IDEA Ultimate

## Требования к окружению 
- JDK 21 
- Maven 3.9 
- PostgreSQL 17 
- JavaFX SDK 21

## Настройка в IntelliJ IDEA Ultimate
1. Открыть проект как Maven-проект
2. В настройках запуска указать VM options: --module-path "путь\javafx-sdk-21\lib" --add-modules=javafx.controls,javafx.fxml
3. JDK 21
4. Запустить главный класс: com.gallery.app.Main

## Пример <img width="1776" height="687" alt="image" src="https://github.com/user-attachments/assets/962c7cbe-cb16-4b5d-9ff8-465764886aa4" />

## Создать db.properties 
src/main/resources/db.properties (пример в db.properties.example)

## Настройка базы данных
Создать базу данных ```sql CREATE DATABASE art_galleries_db;```
Выполнить SQL-скрипт - src/main/resources/init.sql (инициализация данных, без него приложение будет без данных) 
После инициализации базы данных доступен запуск проекта.

--- 
## Тестовые пользователи 
## Администратор 
### Логин: admin 
### Пароль: admin 

## Пользователь 
### Логин: user 
### Пароль: user

---
