package com.gallery.repository;

import com.gallery.config.HibernateUtil;
import com.gallery.entity.PaintingExhibition;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class PaintingExhibitionRepository {

    // Сохранить или обновить связь картины и выставки
    public void save(PaintingExhibition paintingExhibition) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(paintingExhibition);
            tx.commit();
        }
    }

    // Удалить связь картины и выставки
    public void delete(PaintingExhibition paintingExhibition) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(paintingExhibition);  // Удалить связь картины и выставки
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // связи, в которых данная картина пересекается с датами
    public boolean existsOverlap(Long paintingId, LocalDate start, LocalDate end, Integer excludeExhibitionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(pe) FROM PaintingExhibition pe " + "WHERE pe.painting.id = :pid " +
                "AND NOT (pe.endDate < :start OR pe.startDate > :end) ";
            if (excludeExhibitionId != null) {
                hql += "AND pe.exhibition.id <> :exId ";
            }
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("pid", paintingId);
            q.setParameter("start", start);
            q.setParameter("end", end);
            if (excludeExhibitionId != null) {
                q.setParameter("exId", excludeExhibitionId);
            }
            return q.uniqueResult() > 0;
        }
    }

    // Вернуть id картин, занятых в диапазоне (для оптимизации отображения)
    public List<Long> findBusyPaintingIds(LocalDate start, LocalDate end, Integer excludeExhibitionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT pe.painting.id FROM PaintingExhibition pe " +
                "WHERE NOT (pe.endDate < :start OR pe.startDate > :end) ";
            if (excludeExhibitionId != null) {
                hql += "AND pe.exhibition.id <> :exId ";
            }
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            if (excludeExhibitionId != null) q.setParameter("exId", excludeExhibitionId);
            return q.list();
        }
    }

    // Удалить все связи для выставки
    public void deleteByExhibitionId(Integer exhibitionId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<?> q = session.createQuery("DELETE FROM PaintingExhibition pe WHERE pe.exhibition.id = :exId");
            q.setParameter("exId", exhibitionId);
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // Получить все связи картин и выставок
    public List<PaintingExhibition> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM PaintingExhibition", PaintingExhibition.class).list();
        }
    }
}
