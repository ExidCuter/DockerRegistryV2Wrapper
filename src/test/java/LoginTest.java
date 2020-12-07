import org.junit.jupiter.api.Test;
import org.tdfl.docker.DockerRepository;
import org.tdfl.docker.model.Catalog;
import org.tdfl.docker.model.LoginCredentials;
import org.tdfl.docker.model.Manifest;
import org.tdfl.docker.model.Tags;

public class LoginTest {

    private final DockerRepository dockerRepository = new DockerRepository(LoginCredentials.builder()
            .username(System.getProperty("username"))
            .password(System.getProperty("password"))
            .registryURL(System.getProperty("registry"))
            .build()
    );

    @Test
    public void test_getCatalog() {
        Catalog catalog = dockerRepository.getCatalog();

        assert catalog != null;
    }


    @Test
    public void test_getTags() {
        Catalog catalog = dockerRepository.getCatalog();

        Tags tags = dockerRepository.getTags(catalog.getRepositories().get(0));

        assert tags != null;
    }

    @Test
    public void test_getManifest() {
        Catalog catalog = dockerRepository.getCatalog();

        Tags tags = dockerRepository.getTags(catalog.getRepositories().get(0));

        Manifest manifest = dockerRepository.getManifest(catalog.getRepositories().get(0), tags.getTags().get(0));

        assert manifest != null;
    }
}
