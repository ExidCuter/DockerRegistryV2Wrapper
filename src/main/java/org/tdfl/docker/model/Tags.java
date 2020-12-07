package org.tdfl.docker.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tags {

    @JsonProperty("name")
    private String name;
    @JsonProperty("tags")
    private List<String> tags;
}