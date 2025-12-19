package com.gallery.service;

import com.gallery.entity.PaintingExhibition;
import com.gallery.repository.PaintingExhibitionRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PaintingExhibitionService {
    private final PaintingExhibitionRepository paintingExhibitionRepository = new PaintingExhibitionRepository();

    // добавить связь между картиной и выставкой
    public void addPaintingExhibition(PaintingExhibition paintingExhibition) {
        paintingExhibitionRepository.save(paintingExhibition);
    }

    // проверка занятости одной картины
    public boolean isPaintingBusy(Long paintingId, LocalDate start, LocalDate end, Integer excludeExhibitionId) {
        return paintingExhibitionRepository.existsOverlap(paintingId, start, end, excludeExhibitionId);
    }

    // получить set id занятых картин
    public Set<Long> findBusyPaintingIdsBetween(LocalDate start, LocalDate end, Integer excludeExhibitionId) {
        return new HashSet<>(paintingExhibitionRepository.findBusyPaintingIds(start, end, excludeExhibitionId));
    }

    // удалить все связи выставки
    public void deleteByExhibitionId(Integer exhibitionId) {
        paintingExhibitionRepository.deleteByExhibitionId(exhibitionId);
    }
}
