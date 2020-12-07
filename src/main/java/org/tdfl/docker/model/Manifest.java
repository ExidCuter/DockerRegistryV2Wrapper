
package org.tdfl.docker.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Manifest {

    @JsonProperty("schemaVersion")
    private Long schemaVersion;
    @JsonProperty("name")
    private String name;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("architecture")
    private String architecture;
    @JsonProperty("fsLayers")
    private List<FsLayer> fsLayers = null;
    @JsonProperty("history")
    private List<History> history = null;
    @JsonProperty("signatures")
    private List<Signature> signatures = null;
}
