
package org.tdfl.docker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Signature {

    @JsonProperty("header")
    private Header header;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("protected")
    private String _protected;
}
