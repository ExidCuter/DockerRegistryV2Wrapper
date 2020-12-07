
package org.tdfl.docker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Jwk {

    @JsonProperty("crv")
    private String crv;
    @JsonProperty("kid")
    private String kid;
    @JsonProperty("kty")
    private String kty;
    @JsonProperty("x")
    private String x;
    @JsonProperty("y")
    private String y;
}
