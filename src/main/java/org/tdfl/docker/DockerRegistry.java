package org.tdfl.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.*;
import org.tdfl.docker.error.AuthException;
import org.tdfl.docker.error.NotFoundException;
import org.tdfl.docker.error.RegistryErrorException;
import org.tdfl.docker.http.BasicAuthInterceptor;
import org.tdfl.docker.model.Error;
import org.tdfl.docker.model.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Getter
@Setter
public class DockerRegistry {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LoginCredentials loginCredentials;

    public DockerRegistry(LoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder = addCredentialsToBuilder(builder);

        this.client = builder.build();
    }

    public DockerRegistry(LoginCredentials loginCredentials, boolean unsafe) {
        this.loginCredentials = loginCredentials;

        OkHttpClient.Builder builder = unsafe ? getUnsafeOkHttpClientBuilder() : new OkHttpClient.Builder();

        builder = addCredentialsToBuilder(builder);

        this.client = builder.build();
    }

    public DockerRegistry(LoginCredentials loginCredentials, Duration timeout) {
        this.loginCredentials = loginCredentials;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(timeout);

        builder = addCredentialsToBuilder(builder);

        this.client = builder.build();
    }

    public DockerRegistry(LoginCredentials loginCredentials, Duration timeout, boolean unsafe) {
        this.loginCredentials = loginCredentials;

        OkHttpClient.Builder builder = unsafe ? getUnsafeOkHttpClientBuilder() : new OkHttpClient.Builder()
                .readTimeout(timeout);

        builder = addCredentialsToBuilder(builder);

        this.client = builder.build();
    }

    private OkHttpClient.Builder addCredentialsToBuilder(OkHttpClient.Builder builder) {
        if (loginCredentials.getUsername() != null && loginCredentials.getPassword() != null) {
            builder = builder.addInterceptor(new BasicAuthInterceptor(loginCredentials.getUsername(), loginCredentials.getPassword()));
        }

        return builder;
    }

    private static OkHttpClient.Builder getUnsafeOkHttpClientBuilder() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public Catalog getCatalog() {
        return this.getCatalog(0, 100);
    }

    @SneakyThrows
    public Catalog getCatalog(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new InvalidParameterException("pageNumber is supposed to be zero or greater");
        }

        if (pageSize < 1) {
            throw new InvalidParameterException("pageNumber is supposed to be a positive number");
        }

        HttpUrl catalogUrl = HttpUrl.parse(this.loginCredentials.getRegistryURL() + "/v2/_catalog")
                .newBuilder()
                .addQueryParameter("n", Integer.toString(pageSize))
                .addQueryParameter("last", Integer.toString(pageSize * pageNumber))
                .build();


        Request request = new Request.Builder()
                .url(catalogUrl.url())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return getValueFromResponse(response, Catalog.class);
    }

    @SneakyThrows
    public Tags getTags(String repositoryName) {
        return this.getTags(repositoryName, 0, 100);
    }

    @SneakyThrows
    public Tags getTags(String repositoryName, int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new InvalidParameterException("pageNumber is supposed to be zero or greater");
        }

        if (pageSize < 1) {
            throw new InvalidParameterException("pageNumber is supposed to be a positive number");
        }

        HttpUrl tagUrl = HttpUrl.parse(this.loginCredentials.getRegistryURL() + "/v2/" + repositoryName + "/tags/list")
                .newBuilder()
                .addQueryParameter("n", Integer.toString(pageSize))
                .addQueryParameter("last", Integer.toString(pageSize * pageNumber))
                .build();

        Request request = new Request.Builder()
                .url(tagUrl.url())
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
