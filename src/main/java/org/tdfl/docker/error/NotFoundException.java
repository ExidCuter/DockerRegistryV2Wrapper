package org.tdfl.docker.error;

public class NotFoundException extends RegistryErrorException {
    public NotFoundException(String message) {
        super(message);
    }
}
