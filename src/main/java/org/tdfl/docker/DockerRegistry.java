package org.tdfl.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.tdfl.docker.error.AuthException;
import org.tdfl.docker.error.NotFoundException;
import org.tdfl.docker.error.RegistryErrorException;
import org.tdfl.docker.http.BasicAuthInterceptor;
import org.tdfl.docker.model.Catalog;
import org.tdfl.docker.model.Error;
import org.tdfl.docker.model.LoginCredentials;
import org.tdfl.docker.model.Manifest;
import org.tdfl.docker.model.RegistryErrors;
import org.tdfl.docker.model.Tags;

import java.io.IOException;

@Getter
@Setter
public class DockerRegistry {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LoginCredentials loginCredentials;

    public DockerRegistry(LoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;

        if (loginCredentials.getUsername() != null && loginCredentials.getPassword() != null) {
            this.client = new OkHttpClient.Builder()
                    .addInterceptor(new BasicAuthInterceptor(loginCredentials.getUsername(), loginCredentials.getPassword()))
                    .build();
        } else {
            client = new OkHttpClient();
        }
    }

    @SneakyThrows
    public Catalog getCatalog() {
        Request request = new Request.Builder()
                .url(this.loginCredentials.getRegistryURL() + "/v2/_catalog")
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return getValueFromResponse(response, Catalog.class);
    }

    @SneakyThrows
    public Tags getTags(String repositoryName) {
        Request request = new Request.Builder()
                .url(this.loginCredentials.getRegistryURL() + "/v2/" + repositoryName + "/tags/list")
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return getValueFromResponse(response, Tags.class);
    }

    @SneakyThrows
    public Manifest getManifest(String repositoryName, String tag) {
        Request request = new Request.Builder()
                .url(this.loginCredentials.getRegistryURL() + "/v2/" + repositoryName + "/manifests/" + tag)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return getValueFromResponse(response, Manifest.class);
    }

    @SneakyThrows
    public void deleteImage(String repositoryName, String tag) {
        String sha = getSHAOfImage(repositoryName, tag);

        Request request = new Request.Builder()
                .url(this.loginCredentials.getRegistryURL() + "/v2/" + repositoryName + "/manifests/" + sha)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                .delete()
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() != 202) {
            throw new RegistryErrorException(getValueFromResponse(response, String.class));
        }
    }

    @SneakyThrows
    public String getSHAOfImage(String repositoryName, String tag) {
        Request request = new Request.Builder()
                .url(this.loginCredentials.getRegistryURL() + "/v2/" + repositoryName + "/manifests/" + tag)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                .head()
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() != 200) {
            throw new RegistryErrorException(getValueFromResponse(response, RegistryErrors.class).getErrors().get(0).getMessage());
        }

        return response.header("Etag").replaceAll("\"", "");
    }
    private <T> T getValueFromResponse(Response response, Class<T> valueType) throws RegistryErrorException, IOException {
        switch (response.code()) {
            case 200:
                return objectMapper.readValue(response.body().string(), valueType);
            case 401:
                throw new AuthException(this.loginCredentials);
            case 404:
                throw new NotFoundException("");
            default:
                Error error = objectMapper.readValue(response.body().string(), RegistryErrors.class).getErrors().get(0);
                throw new RegistryErrorException("Error: [" + error.getCode() + "] " + error.getMessage());
        }
    }
}
