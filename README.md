# DockerRegistryV2Wrapper - Java Wrapper for Docker Registry API V2

## Summary

DockerRegistryV2Wrapper is a Java wrapper for [Docker Registry V2 API](https://docs.docker.com/registry/spec/api/) 
with Java 11, OkHttp and Jackson.

### Implemented functions:

| Path                             | Method Type | Reference                                                            |
|----------------------------------|-------------|----------------------------------------------------------------------|
| /v2/_catalog                     | GET         | [link](https://docs.docker.com/registry/spec/api/#catalog)           |
| /v2/<name>/tags/list             | GET         | [link](https://docs.docker.com/registry/spec/api/#tags)              |
| /v2/<name>/manifests/<reference> | GET         | [link](https://docs.docker.com/registry/spec/api/#manifest)          |
| /v2/<name>/manifests/<reference> | DELETE      | [link](https://docs.docker.com/registry/spec/api/#deleting-an-image) |

### Errors

| Exception              | Error                                                                            |
|------------------------|----------------------------------------------------------------------------------|
| AuthException          | Problem with authentication (Wrong password or no rights to access the resource) |
| NotFoundException      | The resource does not exists in the repository                                   |
| RegistryErrorException | Generic error for all other possible errors                                      |

## Usage

### Gradle include
Include JitPack repository in the `build.gradle` file:
```java
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
```

Add the dependency:

```java
implementation 'com.github.ExidCuter:DockerRegistryV2Wrapper:master-SNAPSHOT'
```

### Example code

```java
DockerRepository dockerRepository = new DockerRepository(
        LoginCredentials.builder()
                .username("username")
                .password("password")
                .registryURL("registry")
                .build()
);

Catalog catalog = dockerRepository.getCatalog();

Tags tags = dockerRepository.getTags("amazoncorretto");

Manifest manifest = dockerRepository.getManifest("amazoncorretto", "11");

dockerRepository.deleteImage("amazoncorretto", "11");
```
