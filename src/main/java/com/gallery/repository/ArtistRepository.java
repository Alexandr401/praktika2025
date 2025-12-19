package com.gallery.repository;

import com.gallery.config.HibernateUtil;
import com.gallery.entity.Artist;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtistRepository {
    private static final Logger log = LoggerFactory.getLogger(ArtistRepository.class);

    // сохранить или обновить автора
    public void save(Artist artist) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(artist);
            tx.commit();
        }
    }

    // получить всех авторов
    public List<Artist> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Artist", Artist.class).list();
        }
    }

    // найти автора по id
    public Artist findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Artist.class, id);
        }
    }

    // удалить автора
    public void delete(Artist artist) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(artist);  // удалить автора из базы
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Ошибка при удалении автора: {}", artist, e);
        }
    }
}
