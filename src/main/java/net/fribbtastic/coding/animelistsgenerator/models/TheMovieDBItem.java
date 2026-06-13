package net.fribbtastic.coding.animelistsgenerator.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Frederic Eßer
 */
@Data
public class TheMovieDBItem {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("movie")
    private List<Integer> movie;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tv")
    private Integer tv;
}
