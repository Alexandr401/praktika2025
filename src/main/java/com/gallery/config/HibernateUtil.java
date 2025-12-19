package com.gallery.config;

import com.gallery.entity.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Загрузка db.properties
            Properties props = new Properties();
            InputStream input = HibernateUtil.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties");

            if (input == null) {
                throw new RuntimeException("Файл db.properties не найден в classpath");
            }
            props.load(input);


            Configuration configuration = new Configuration();

            // JDBC настройки
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://" + props.getProperty("db.host") + ":" + props.getProperty("db.port") + "/" + props.getProperty("db.name"));
            configuration.setProperty("hibernate.connection.username", props.getProperty("db.user"));
            configuration.setProperty("hibernate.connection.password", props.getProperty("db.password"));

            // Настройки Hibernate
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update"); // создание таблиц если их нет

            // Подключение сущности
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Artist.class);
            configuration.addAnnotatedClass(Painting.class);
            configuration.addAnnotatedClass(Exhibition.class);
            configuration.addAnnotatedClass(PaintingExhibition.class);

            return configuration.buildSessionFactory();
        } catch (Exception ex) {
            logger.error("Ошибка при инициализации SessionFactory", ex);
            throw new RuntimeException("Ошибка при инициализации SessionFactory", ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
