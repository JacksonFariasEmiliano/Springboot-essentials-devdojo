package com.jfalves.springboot.integration;

import com.jfalves.springboot.domain.Anime;
import com.jfalves.springboot.domain.DevDojoUser;
import com.jfalves.springboot.repository.AnimeRepository;
import com.jfalves.springboot.repository.DevDojoUserRepository;
import com.jfalves.springboot.requests.AnimePostRequestBody;
import com.jfalves.springboot.util.AnimeCreator;
import com.jfalves.springboot.util.AnimePostRequestBodyCreator;
import com.jfalves.springboot.wrapper.PageableResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnimeControllerIT {

    @Autowired
    @Qualifier(value = "testRestTemplateRoleUser")
    private TestRestTemplate testRestTemplate;

//    @LocalServerPort
//    private int port;

    @Autowired
    private AnimeRepository repository;

    @Autowired
    private DevDojoUserRepository devDojoUserRepository;

    @Lazy
    @TestConfiguration
    static class Config{

        @Bean(name = "testRestTemplateRoleUser")
        public  TestRestTemplate testRestTemplateRoleUserCreator(@Value("${local.server.port}") int port){
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                    .rootUri("http://localhost:"+port)
                    .basicAuthentication("jackson", "root");
            return new TestRestTemplate(restTemplateBuilder);
        }
    }

    @Test
    @DisplayName("List return list of anime inside page object when successful")
    void listReturnsListOfAnimesInsidePageObjectWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());
        DevDojoUser user = DevDojoUser.builder()
                .name("Creator Jackson")
                .password("{bcrypt}$2a$10$k/GAWBW3XDpv0F4uXvFyHutvVnwQv5NQ73OJ.t17QCVX0Qrxl1tlK")
                .username("jackson")
                .authorities("ROLE_USER")
                .build();
        devDojoUserRepository.save(user);
        String expectedName = savedAnime.getName();

        PageableResponse<Anime> animePage = testRestTemplate.exchange("/animes", HttpMethod.GET, null,
                new ParameterizedTypeReference<PageableResponse<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull();

        Assertions.assertThat(animePage.toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animePage
                        .toList()
                        .get(0)
                        .getName())
                .isEqualTo(expectedName);
    }

    @Test
    @DisplayName("List all returns list of anime when successful")
    void listAllReturnsListOfAnimesWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();

        List<Anime> animeList = testRestTemplate.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();


        Assertions.assertThat(animeList).isNotNull();

        Assertions.assertThat(animeList)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animeList.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Find by id returns anime when successful")
    void findByIdReturnsAnimesWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());

        Long expectedId = savedAnime.getId();

        Anime anime = testRestTemplate.getForObject("/animes/{id}", Anime.class, expectedId);

        Assertions.assertThat(anime).isNotNull();

        Assertions.assertThat(anime.getId()).isNotNull().isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Find by name returns a list anime when successful")
    void findByNameReturnsListOfAnimesWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();
        String url = String.format("/animes/find?name=%s", expectedName);

        List<Anime> animeList = testRestTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animeList)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animeList.get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Find by name returns an empty list of anime when anime is not found")
    void findByNameReturnsEmptyListOfAnimesWhenAnimeIsNotFound() {
        List<Anime> animeEmpty = testRestTemplate.exchange("/animes/find?name=dbz", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animeEmpty)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Save returns anime when successful")
    void saveReturnsAnimesWhenSuccessful() {
        AnimePostRequestBody animeRequestBody = AnimePostRequestBodyCreator.createAnimePostRequestBody();

        ResponseEntity<Anime> animeResponseEntity = testRestTemplate.postForEntity("/animes", animeRequestBody, Anime.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(animeResponseEntity.getBody()).isNotNull();
        Assertions.assertThat(animeResponseEntity.getBody().getId()).isNotNull();
        Assertions.assertThat(animeResponseEntity.getBody().getName()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Replace updates anime when successful")
    void replaceUpdatesAnimesWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());

        savedAnime.setName("new name");
        Long expectedId = savedAnime.getId();

        ResponseEntity<Void> animeResponseEntity = testRestTemplate.exchange("/animes/{id}", HttpMethod.PUT, new HttpEntity<>(savedAnime), Void.class, expectedId);

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    @DisplayName("Delete anime when successful")
    void deleteAnimesWhenSuccessful() {
        Anime savedAnime = repository.save(AnimeCreator.createAnimeToBeSaved());

        Long expectedId = savedAnime.getId();

        ResponseEntity<Void> animeResponseEntity = testRestTemplate.exchange("/animes/{id}",
                HttpMethod.DELETE, null, Void.class, savedAnime.getId());


        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
