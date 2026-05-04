package net.fribbtastic.coding.animelistsgenerator.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * @author Frederic Eßer
 */
@Data
@AllArgsConstructor
@Getter
public class AnimeCollection {

    private String name;
    private List<String> idList;
}
