# REST DataStore

## Testing

The `jmix-restds` module runs tests against the `sample-rest-service` app. Due to the dependency to a separately running application, the tests are launched only when the `runRestDsTests` Gradle property is set to `true`. 

If you run `jmix-restds` tests frequently and have Testcontainers available or manually launch the service, add `runRestDsTests` property to your `~/.gradle/gradle.properties` file:

```properties
runRestDsTests=true
```

### Using Testcontainers

If Docker is available and configured to run Testcontainers, the `sample-rest-service` app can be run in a container. This is the default configuration, so you can run tests from IDE or from the command line without specifying any additional options except `runRestDsTests=true`.  

Running all tests from the `jmix` root directory:

```shell
cd jmix
./gradlew -PrunRestDsTests=true test
```

Running `jmix-restds` tests:

```shell
cd jmix
./gradlew -PrunRestDsTests=true :restds:test
```

### Running Sample Service Manually

If you cannot (or don't want to) use Testcontainers, run the sample service manually:

```shell
cd jmix
./gradlew :sample-rest-service:bootRun
```

Then provide the `useStandaloneServiceForRestDsTests` Gradle property when running the tests. You can do it on the command line:

```shell
cd jmix
./gradlew -PrunRestDsTests=true -PuseStandaloneServiceForRestDsTests=true :restds:test
```

Or by adding the `useStandaloneServiceForRestDsTests` Gradle property to your `~/.gradle/gradle.properties` file:

```properties
useStandaloneServiceForRestDsTests=true
```
