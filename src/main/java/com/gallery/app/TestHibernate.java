package com.gallery.app;

import com.gallery.config.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHibernate {
    private static final Logger logger = LoggerFactory.getLogger(TestHibernate.class);

    public static void main(String[] args) {
        // Проверка подключения
        try (Session ignored = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Подключение к PostgreSQL успешно!");
        } catch (Exception e) {
            logger.error("Ошибка при подключении к PostgreSQL", e);
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
