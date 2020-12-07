package org.tdfl.docker.error;

import org.tdfl.docker.model.LoginCredentials;

public class AuthException extends RegistryErrorException {
    public AuthException(LoginCredentials loginCredentials) {
        super("Error authenticating with: " + loginCredentials);
    }
}
