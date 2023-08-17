import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tdfl.docker.DockerRegistry;
import org.tdfl.docker.model.Catalog;
import org.tdfl.docker.model.LoginCredentials;
import org.tdfl.docker.model.Manifest;
import org.tdfl.docker.model.Tags;

public class LoginTest {

    private final DockerRegistry dockerRegistry = new DockerRegistry(LoginCredentials.builder()
            .username(System.getProperty("username"))
            .password(System.getProperty("password"))
            .registryURL(System.getProperty("registry"))
            .build()
    );

    @Test
    public void test_getCatalog() {
        Catalog catalog = dockerRegistry.getCatalog(0, 400);

        assert catalog != null;
    }

    @Test
    public void test_getTags() {
        Catalog catalog = dockerRegistry.getCatalog();

        Tags tags = dockerRegistry.getTags(catalog.getRepositories().get(0), 0, 400);

        assert tags != null;
    }

    @Test
    public void test_getManifest() {
        Catalog catalog = dockerRegistry.getCatalog();

        Tags tags = dockerRegistry.getTags(catalog.getRepositories().get(0));

        Manifest manifest = dockerRegistry.getManifest(catalog.getRepositories().get(0), tags.getTags().get(0));

        assert manifest != null;
    }

    @Test
    @Disabled
    public void test_deleteImage() {
        dockerRegistry.deleteImage("umu-be", "develop-20201004-81bd5cd4");
    }
}
