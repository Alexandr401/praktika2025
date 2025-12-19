package com.gallery.service;

import com.gallery.entity.Painting;
import com.gallery.repository.PaintingRepository;

import java.util.List;

public class PaintingService {
    private final PaintingRepository paintingRepository = new PaintingRepository();

    public List<Painting> getAllPaintings() {
        return paintingRepository.findAll();
    }

    public void savePainting(Painting painting) {
        paintingRepository.save(painting);
    }

    public void deletePainting(Painting painting) {
        paintingRepository.delete(painting);
    }
}
