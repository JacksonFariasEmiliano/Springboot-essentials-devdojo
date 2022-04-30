package com.jfalves.springboot.service;

import com.jfalves.springboot.domain.Anime;
import com.jfalves.springboot.exception.BadRequestException;
import com.jfalves.springboot.mapper.AnimeMapper;
import com.jfalves.springboot.repository.AnimeRepository;
import com.jfalves.springboot.requests.AnimePostRequestBody;
import com.jfalves.springboot.requests.AnimePutRequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository repository;

    public Page<Anime> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Anime> listAllNoPageable() {
        return repository.findAll();
    }

    public List<Anime> findByName(String name) {
        return repository.findByName(name);
    }

    public Anime findByIdOrThrowBadRequestException(long id) {
        return repository.findById(id).orElseThrow(() -> new BadRequestException("Anime not found"));
    }

    @Transactional
    public Anime save(AnimePostRequestBody animePostRequestBody) {
        return repository.save(AnimeMapper.INSTANCE.toAnime(animePostRequestBody));
        //return repository.save(Anime.builder().name(animePostRequestBody.getName()).build());
    }

    public void delete(long id) {
        repository.delete(findByIdOrThrowBadRequestException(id));
    }

    public void replace(AnimePutRequestBody animePutRequestBody) {
        Anime savedAnime = findByIdOrThrowBadRequestException(animePutRequestBody.getId());
        Anime anime = AnimeMapper.INSTANCE.toAnime(animePutRequestBody);
        anime.setId(savedAnime.getId());
        repository.save(anime);

//        Anime anime = Anime.builder()
//                .id(savedAnime.getId())
//                .name(animePutRequestBody.getName())
//                .build();
//
//        repository.save(anime);
    }


}
