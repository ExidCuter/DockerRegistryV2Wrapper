package org.tdfl.docker.error;

public class RegistryErrorException extends Exception {
    public RegistryErrorException(String message) {
        super(message);
    }
}
