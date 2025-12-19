package com.gallery.service;

import com.gallery.entity.Artist;
import com.gallery.repository.ArtistRepository;
import java.util.List;

public class ArtistService {

    private ArtistRepository artistRepository = new ArtistRepository();

    // Добавить нового автора
    public void addArtist(Artist artist) {
        artistRepository.save(artist);
    }

    // Обновить информацию об авторе
    public void updateArtist(Artist artist) {
        artistRepository.save(artist);  // Hibernate обновит запись
    }

    // Удалить автора
    public void deleteArtist(Artist artist) {
        artistRepository.delete(artist);
    }

    // Получить всех авторов
    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    // Найти автора по ID
    public Artist getArtistById(int id) {
        return artistRepository.findById(id);
    }
}
