package net.fribbtastic.coding.animelistsgenerator.animeLists.service;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
@ActiveProfiles("test-full")
@ExtendWith(MockitoExtension.class)
class AnimeListsServiceTest {

    @Autowired
    private AnimeListsService animeListsService;

    @Test
    @DisplayName("Test: Full anime-lists")
    public void testGetAnimeLists() {
        ArrayList<AnimeItem> list = this.animeListsService.generateList();

        Assertions.assertThat(list).isNotNull();
        Assertions.assertThat(list.size()).isEqualTo(10403);

        Assertions.assertThat(list.getFirst()).isNotNull();
        Assertions.assertThat(list.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(list.getFirst().getTvdb()).isEqualTo(72025);
        Assertions.assertThat(list.getFirst().getTheMovieDb()).isNotNull();
        Assertions.assertThat(list.getFirst().getTheMovieDb().getMovie()).isNull();
        Assertions.assertThat(list.getFirst().getTheMovieDb().getTv()).isEqualTo(26209);

        Assertions.assertThat(list.get(45)).isNotNull();
        Assertions.assertThat(list.get(45).getAnidb()).isEqualTo(49);
        Assertions.assertThat(list.get(45).getTvdb()).isEqualTo(70861);
        Assertions.assertThat(list.get(45).getTheMovieDb().getTv()).isEqualTo(30992);
        Assertions.assertThat(list.get(45).getSeason().getThetvdb()).isEqualTo(0);
        Assertions.assertThat(list.get(45).getEpisodeOffset().getThetvdb()).isEqualTo(2);
        Assertions.assertThat(list.get(45).getEpisodeOffset().getTheMovieDb()).isEqualTo(2);
    }

}