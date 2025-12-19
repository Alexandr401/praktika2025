package com.gallery.repository;

import com.gallery.config.HibernateUtil;
import com.gallery.entity.Exhibition;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExhibitionRepository {
    private static final Logger log = LoggerFactory.getLogger(ExhibitionRepository.class);

    // сохранить или обновить выставку
    public void save(Exhibition exhibition) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(exhibition);
            tx.commit();
        }
    }

    // получить все выставки
    public List<Exhibition> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Exhibition", Exhibition.class).list();
        }
    }

    // удалить выставку
    public void delete(Exhibition exhibition) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(exhibition); // удалить выставку из базы
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Ошибка при выставки автора: {}", exhibition, e);
        }
    }

    // найти выставку по ID
    public Exhibition findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Exhibition.class, id);
        }
    }
}
