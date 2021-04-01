package org.tdfl.docker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistryErrors {
    @JsonProperty("errors")
    private List<Error> errors = null;
}
