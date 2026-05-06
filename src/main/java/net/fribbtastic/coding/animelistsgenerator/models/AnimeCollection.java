package net.fribbtastic.coding.animelistsgenerator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("name")
    private String name;
    @JsonProperty("ids")
    private List<Object> idList;
}
