package org.tdfl.docker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
}
