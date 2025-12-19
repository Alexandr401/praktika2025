package com.gallery.repository;

import com.gallery.config.HibernateUtil;
import com.gallery.entity.Painting;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class PaintingRepository {

    // Сохранить или обновить картину
    public void save(Painting painting) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(painting);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // Получить все картины
    public List<Painting> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Painting", Painting.class).list();
        }
    }

    // Удалить картину
    public void delete(Painting painting) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(painting); // Удалить картину из базы
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
