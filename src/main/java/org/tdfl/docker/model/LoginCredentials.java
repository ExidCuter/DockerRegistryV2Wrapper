package org.tdfl.docker.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginCredentials {

    private String username;
    private String password;
    private String registryURL;
}
