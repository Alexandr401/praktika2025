package com.gallery.service;

import com.gallery.entity.Exhibition;
import com.gallery.repository.ExhibitionRepository;
import com.gallery.config.HibernateUtil;
import java.util.List;

public class ExhibitionService {

    private ExhibitionRepository exhibitionRepository = new ExhibitionRepository();

    // сохранить или обновить выставку
    public Exhibition save(Exhibition exhibition) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            // saveOrUpdate, чтобы Hibernate сам решал insert/update
            session.saveOrUpdate(exhibition);
            tx.commit();
        }

        // объект exhibition уже имеет ID, можно безопасно создавать связи PaintingExhibition
        return exhibition;
    }

    // Удалить выставку
    public void deleteExhibition(Exhibition exhibition) {
        exhibitionRepository.delete(exhibition);
    }

    // Получить все выставки
    public List<Exhibition> getAllExhibitions() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                "SELECT DISTINCT e FROM Exhibition e " +
                    "LEFT JOIN FETCH e.paintingExhibitions pe " +
                    "LEFT JOIN FETCH pe.painting p",
                Exhibition.class
            ).getResultList();
        }
    }

    // Найти выставку по ID
    public Exhibition getExhibitionById(int id) {
        return exhibitionRepository.findById(id);
    }
}
