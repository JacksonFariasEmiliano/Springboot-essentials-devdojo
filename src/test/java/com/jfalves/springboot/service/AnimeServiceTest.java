package com.jfalves.springboot.service;

import com.jfalves.springboot.domain.Anime;
import com.jfalves.springboot.exception.BadRequestException;
import com.jfalves.springboot.repository.AnimeRepository;
import com.jfalves.springboot.util.AnimeCreator;
import com.jfalves.springboot.util.AnimePostRequestBodyCreator;
import com.jfalves.springboot.util.AnimePutRequestBodyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    //Quando quer testar a classe em si
    @InjectMocks
    private AnimeService service;

    //Utilizado para testar todas as classes que estão dentro do repository
    @Mock
    private AnimeRepository repository;

    @BeforeEach
    void setUp() {
        PageImpl<Anime> animePage = new PageImpl<>(List.of(AnimeCreator.createValidAnime()));

        BDDMockito.when(repository.findAll(ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(animePage);

        BDDMockito.when(repository.findAll())
                .thenReturn(List.of(AnimeCreator.createValidAnime()));

        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(AnimeCreator.createValidAnime()));

        BDDMockito.when(repository.findByName(ArgumentMatchers.anyString()))
                .thenReturn(List.of(AnimeCreator.createValidAnime()));

        BDDMockito.when(repository.save(ArgumentMatchers.any(Anime.class)))
                .thenReturn(AnimeCreator.createValidAnime());

        BDDMockito.doNothing().when(repository).delete(ArgumentMatchers.any(Anime.class));
    }

    @Test
    @DisplayName("List returns list of anime inside page object when successful")
    void listAllReturnsListOfAnimesInsidePageObjectWhenSuccessful() {
        String expectedName = AnimeCreator.createValidAnime().getName();
        Page<Anime> animePage = service.listAll(PageRequest.of(1,1));

        Assertions.assertThat(animePage).isNotNull();
        Assertions.assertThat(animePage.toList()).isNotEmpty().hasSize(1);
        //Assertions.assertThat(animePage.toList()).isEqualTo(expectedName);
        Assertions.assertThat(animePage.toList().get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("List all noPageable returns list of anime when successful")
    void listAllNoPageableReturnsListOfAnimesWhenSuccessful() {
        String expectedName = AnimeCreator.createValidAnime().getName();
        List<Anime> animes = service.listAllNoPageable();

        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Find by id or throw bad request exception anime when successful")
    void findByIdOrThrowBadRequestExceptionAnimesWhenSuccessful() {
        Long expectedId = AnimeCreator.createValidAnime().getId();

        Anime anime = service.findByIdOrThrowBadRequestException(1);

        Assertions.assertThat(anime).isNotNull();

        Assertions.assertThat(anime.getId()).isNotNull().isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Find by id or throw bad request exception  throws bad request exception when anime is not found ")
    void findByIdOrThrowBadRequestExceptionThrowsBadRequestExceptionWhenAnimeIsNotFound() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(BadRequestException.class).isThrownBy(()-> service.findByIdOrThrowBadRequestException(1));
    }

    @Test
    @DisplayName("Find by name returns list of anime when successful")
    void findByNameReturnsListOfAnimesWhenSuccessful() {
        String expectedName = AnimeCreator.createValidAnime().getName();
        List<Anime> animes = service.findByName("anime");

        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Find by name returns an empty list of anime when anime is not found")
    void findByNameReturnsEmptyListOfAnimesWhenAnimeIsNotFound() {
        BDDMockito.when(repository.findByName(ArgumentMatchers.anyString())).thenReturn(Collections.emptyList());

        List<Anime> animes = service.findByName("anime");

        Assertions.assertThat(animes)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Save returns anime when successful")
    void saveReturnsAnimesWhenSuccessful() {
        Anime anime = service.save(AnimePostRequestBodyCreator.createAnimePostRequestBody());
        Assertions.assertThat(anime).isNotNull();
        Assertions.assertThat(anime.getName()).isEqualTo(AnimeCreator.createValidAnime().getName());
    }

    @Test
    @DisplayName("Replace updates anime when successful")
    void replaceUpdatesAnimesWhenSuccessful() {
        Assertions.assertThatCode(() -> service.replace(AnimePutRequestBodyCreator.createAnimePutRequestBody()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Delete anime when successful")
    void deleteAnimesWhenSuccessful() {
        Assertions.assertThatCode(() -> service.delete(1))
                .doesNotThrowAnyException();
    }

}